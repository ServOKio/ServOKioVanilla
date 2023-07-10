package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import net.servokio.vanilla.modules.Static;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class MHeaderImage implements MMain{
    private static XSharedPreferences mPrefs;

    protected int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB

    private LinearLayout headerImageRoot = null;

    private ImageView mHeaderImageView = null;
    private TextureView mHeaderVideoView = null;
    private MediaPlayer mMediaPlayer;

    private float mVideoWidth;
    private float mVideoHeight;

    private Object oldQSBH = null;
    private View oldmStatusBarBackground = null;

    private void updateQSBH(Object obj){
        //QuickStatusBarHeader
        try{
            if(oldQSBH == null) {
                oldQSBH = obj;
            } else if(obj == null){
                obj = oldQSBH;
            }

            if(obj != null){
                if(obj instanceof FrameLayout){
                    FrameLayout v = (FrameLayout) obj;
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
                    lp.height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                    v.setLayoutParams(lp);
                } else if(obj instanceof LinearLayout){
                    LinearLayout v = (LinearLayout) obj;
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    lp.height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                    v.setLayoutParams(lp);
                }
            } else XposedBridge.log("Vanilla error: updateQSBH is null");
        } catch (Exception e){
            XposedBridge.log("Error updateQSBH: "+e.getMessage());
        }
    }

    private void updateQSCI(View old){
        try{
            if(old != null){
                oldmStatusBarBackground = old;
            } else if(oldmStatusBarBackground != null) old = oldmStatusBarBackground;
            if(old != null) {
                int fsd = mPrefs.getInt("status_bar_custom_header_height", 25);

                //new
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) oldmStatusBarBackground.getLayoutParams();
                lp.height = Static.dpToPx(oldmStatusBarBackground.getContext(), fsd);
                oldmStatusBarBackground.setLayoutParams(lp);
                XposedBridge.log("Vanilla: updateQSCI done for "+fsd);
            } else XposedBridge.log("Vanilla error: updateQSCI is null");
        } catch (Exception e){
            XposedBridge.log("Error updateQSCI: "+e.getMessage());
        }
    }

    private int getHeaderImageHeight(){
        return Static.dpToPx(oldmStatusBarBackground.getContext(), MHeaderImage.mPrefs.getInt("status_bar_custom_header_height", 25));
    }

    public void updateHeaderHeights(){
        XposedBridge.log("Vanilla: update updateHeaderHeights");
        updateQSBH(null);
        updateQSCI(null);
    }

    public void updateHeaderImage(){
        //Blyat
        Log.d("Vanilla", "run updateHeaderImage");
        // 1. Check if active and stop
        stopVideoHeader();

        // 2. Check
        String hfmine = mPrefs.getString("status_bar_custom_header_image_type", "unk");
        File file = new File(mPrefs.getFile().getParent() + "/custom_file_header_image");
        if(!hfmine.equals("unk") && file.exists()){
            //Стартуем
            Log.d("Vanilla", "run updateHeaderImage ok");
            if(hfmine.equals("video/mp4")){
                mHeaderVideoView.setVisibility(View.VISIBLE);
                mHeaderImageView.setVisibility(View.GONE);

                calculateVideoSize(file);
                mHeaderVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                        MHeaderImage.this.onSurfaceTextureAvailable(surfaceTexture, i, i1);
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

                if (mHeaderVideoView.isAvailable()) onSurfaceTextureAvailable(mHeaderVideoView.getSurfaceTexture(), mHeaderVideoView.getWidth(), mHeaderVideoView.getHeight());

                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setVolume(0,0);
                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                    mMediaPlayer.setLooping(true);

                    mMediaPlayer.prepare();

                    mMediaPlayer.setOnPreparedListener(MediaPlayer::start);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                mHeaderImageView.setVisibility(View.VISIBLE);
                mHeaderVideoView.setVisibility(View.GONE);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                //Image types
                if(hfmine.equals("image/gif") || bitmap.getByteCount() > MAX_BITMAP_SIZE){
                    Glide.with(headerImageRoot.getContext()).load(file).into(mHeaderImageView);
                } else mHeaderImageView.setImageBitmap(bitmap);
            }
        }

    }

    public void updatePrefs(){
        mPrefs.reload();
    }

    private void stopVideoHeader(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void initLoad(final XSharedPreferences xSharedPreferences, final ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        //Done
        findAndHookMethod("com.android.systemui.qs.QuickStatusBarHeader", classLoader, "updateResources", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                Object obj = XposedHelpers.getObjectField(methodHookParam.thisObject, "mSystemIconsView");
                updateQSBH(obj);
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "onFinishInflate", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;

                updateQSCI((View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground"));

                // Фактически щас пихаем всё и скрываем

                // 1. Блок с говном
                headerImageRoot = new LinearLayout(mQSContainerImpl.getContext());
                headerImageRoot.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getHeaderImageHeight()
                ));
                headerImageRoot.setOrientation(LinearLayout.HORIZONTAL);
                int ma = Static.dpToPx(mQSContainerImpl.getContext(), 4);
                headerImageRoot.setPadding(ma, ma, ma, ma);

                // 2. Image
                mHeaderImageView = new ImageView(headerImageRoot.getContext());
                mHeaderImageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
                mHeaderImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //add*
                headerImageRoot.addView(mHeaderImageView, 0);
                mHeaderImageView.setVisibility(View.GONE);

                // 3. Video
                mHeaderVideoView = new TextureView(headerImageRoot.getContext());
                mHeaderVideoView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
                //add*
                headerImageRoot.addView(mHeaderVideoView, 0);
                mHeaderVideoView.post(()->{
                    updateTextureViewSize(headerImageRoot.getWidth(), getHeaderImageHeight());
                });
                mHeaderVideoView.setVisibility(View.GONE);

                // 4. Insert and done
                //add* final
                mQSContainerImpl.addView(headerImageRoot, 1);

                updateHeaderImage();
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSFragment", classLoader, "onDestroy", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                stopVideoHeader();
            }
        });



        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "updateResources", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;
                View mStatusBarBackground = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground");
                int bh = Static.dpToPx(mStatusBarBackground.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));

                View mQSPanelContainer = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mQSPanelContainer");
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mQSPanelContainer.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });
    }

    private void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayer.setSurface(surface);
    }

    public void initInit(XResources res){

        res.hookLayout("com.android.systemui", "layout", "qs_panel", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                View background = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_status_bar_background", "id", "com.android.systemui"));
                if(mPrefs.getBoolean("qs_header_transparency", false)) background.setBackgroundColor(0x00000000);
                View gradient = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_gradient_view", "id", "com.android.systemui"));
                if(gradient != null) gradient.setVisibility(View.GONE);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_customize_panel_content", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));

                View customizer_transparent_view = liparam.view.findViewById(liparam.res.getIdentifier("customizer_transparent_view", "id", "com.android.systemui"));
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) customizer_transparent_view.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_detail", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mContainer.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });
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

        mHeaderVideoView.setTransform(matrix);
        mHeaderVideoView.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
    }
}
