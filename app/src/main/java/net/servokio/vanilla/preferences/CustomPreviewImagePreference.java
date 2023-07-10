package net.servokio.vanilla.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.bumptech.glide.Glide;

import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.modules.mods.MHeaderImage;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XposedBridge;

public class CustomPreviewImagePreference extends Preference {
    protected ImageView imagePreview = null;
    protected TextureView texturePreview = null;
    protected LinearLayout box = null;
    protected SharedPreferences.OnSharedPreferenceChangeListener listener;
    protected int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB

    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private float mVideoWidth;
    private float mVideoHeight;

    protected String filePath = "";

    public CustomPreviewImagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomPreviewImagePreference, defStyleAttr, defStyleRes);
        try {
            String path = a.getString(R.styleable.CustomPreviewImagePreference_path);
            if (path != null){
                System.out.println("ok "+path);
                filePath = path;
            } else System.out.println("CustomPreviewImagePreference_path - not ok");
        } finally {
            a.recycle();
        }

        listener = (prefs, key) -> {
            if(box != null && key.equals("status_bar_custom_header_height")){
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) box.getLayoutParams();
                lp.height = Static.dpToPx(box.getContext(), getSharedPreferences().getInt("status_bar_custom_header_height", 25));
                box.setLayoutParams(lp);
            }
            if((imagePreview != null || texturePreview != null) && key.contains("status_bar_custom_header")) updateViews();
        };

        setLayoutResource(R.layout.presence_custom_preview_image);
    }

    public CustomPreviewImagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public CustomPreviewImagePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle));
    }

    public void setFilePath(String path) {
        this.filePath = path;
        Log.d("Vanilla", "New path banner: " + path);
    }

    @Override
    public void onDetached(){
        super.onDetached();
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public CustomPreviewImagePreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        box = (LinearLayout) holder.findViewById(R.id.value_frame);
        if(box != null){
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) box.getLayoutParams();
            lp.height = Static.dpToPx(box.getContext(), getSharedPreferences().getInt("status_bar_custom_header_height", 25));
            box.setLayoutParams(lp);
        }

        //check
        imagePreview = (ImageView) holder.findViewById(R.id.image_preview);

        texturePreview = (TextureView) holder.findViewById(R.id.texture_preview);
        texturePreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                CustomPreviewImagePreference.this.onSurfaceTextureAvailable(surfaceTexture, i, i1);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
        if (texturePreview.isAvailable()) onSurfaceTextureAvailable(texturePreview.getSurfaceTexture(), texturePreview.getWidth(), texturePreview.getHeight());
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        texturePreview.post(this::updateViews);
    }

    private void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface surface = new Surface(surfaceTexture);
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        System.out.println("New surface");
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setVolume(0,0);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);

            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void updateViews() {
        File f = new File(filePath);
        Log.d("Vanilla", "updateViews in CustomPreview");
        if(f.exists()){
            try {
                String mine = getSharedPreferences().getString("status_bar_custom_header_image_type", "unk");
                if(mine.equals("video/mp4")){
                    calculateVideoSize(f);
                    updateTextureViewSize((int) box.getWidth(), (int) box.getHeight());
                    texturePreview.setVisibility(View.VISIBLE);
                    imagePreview.setVisibility(View.GONE);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    if (bitmap.getByteCount() > MAX_BITMAP_SIZE || getSharedPreferences().getString("status_bar_custom_header_image_type", "unk").equals("image/gif")) {
                        Glide.with(box.getContext()).load(f).into(imagePreview);
                    } else imagePreview.setImageBitmap(bitmap);
                    imagePreview.setVisibility(View.VISIBLE);
                    texturePreview.setVisibility(View.GONE);
                }
            } catch (RuntimeException e){
                imagePreview.setImageDrawable(new ColorDrawable(0x00000000));
                texturePreview.setVisibility(View.GONE);
            }
        } else Log.d("Vanilla", "path is null ?");
    }

    private void calculateVideoSize(File file) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(file.getAbsolutePath());
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mVideoHeight = Float.parseFloat(height);
            mVideoWidth = Float.parseFloat(width);
        } catch (NumberFormatException e) {
            XposedBridge.log("Error MHeaderImage: "+e.getMessage());
        }
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        if (mVideoWidth > viewWidth && mVideoHeight > viewHeight) {
            scaleX = mVideoWidth / viewWidth;
            scaleY = mVideoHeight / viewHeight;
        } else if (mVideoWidth < viewWidth && mVideoHeight < viewHeight) {
            scaleY = viewWidth / mVideoWidth;
            scaleX = viewHeight / mVideoHeight;
        } else if (viewWidth > mVideoWidth) {
            scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
        } else if (viewHeight > mVideoHeight) {
            scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
        }

        // Calculate pivot points, in our case crop from center
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        texturePreview.setTransform(matrix);
        texturePreview.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
    }
}
