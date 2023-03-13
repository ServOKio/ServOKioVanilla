package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private XSharedPreferences mPrefs;
    private XSharedPreferences mPrefs2;

    protected int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB

    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;

    private float mVideoWidth;
    private float mVideoHeight;

    public void initLoad(final XSharedPreferences xSharedPreferences, final ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        findAndHookMethod("com.android.systemui.qs.QuickStatusBarHeader", classLoader, "updateResources", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                Object obj = XposedHelpers.getObjectField(methodHookParam.thisObject, "mSystemIconsView");
                if(obj instanceof FrameLayout){
                    FrameLayout v = (FrameLayout) obj;
                    v.getLayoutParams().height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                } else if(obj instanceof LinearLayout){
                    LinearLayout v = (LinearLayout) obj;
                    v.getLayoutParams().height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                }
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "onFinishInflate", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;
                View mStatusBarBackground = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground");
                int bh = Static.dpToPx(mStatusBarBackground.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                mStatusBarBackground.getLayoutParams().height = bh;

                LinearLayout ll = new LinearLayout(mQSContainerImpl.getContext());
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        bh
                ));
                ll.setOrientation(LinearLayout.HORIZONTAL);
                int ma = Static.dpToPx(mQSContainerImpl.getContext(), 4);
                ll.setPadding(ma, ma, ma, ma);

                String hfmine = mPrefs.getString("status_bar_custom_header_image_type", "unk");

                if(!hfmine.equals("unk")){

                    File file = new File(mPrefs.getFile().getParent() + "/custom_file_header_image");
                    if (file.exists()) {
                        if(hfmine.equals("video/mp4")){
                            mTextureView = new TextureView(ll.getContext());
                            mTextureView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            ));
                            ll.addView(mTextureView, 0);
                            mQSContainerImpl.addView(ll, 1);
                            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
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
                            if (mTextureView.isAvailable()) onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
                            try {
                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setVolume(0,0);
                                mMediaPlayer.setDataSource(file.getAbsolutePath());
                                mMediaPlayer.setLooping(true);

                                mMediaPlayer.prepare();

                                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        mediaPlayer.start();
                                    }
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mTextureView.post(()->{
                                calculateVideoSize(file);
                                updateTextureViewSize(ll.getWidth(), bh);
                            });
                        } else {
                            //Image
                            ImageView iv = new ImageView(ll.getContext());
                            iv.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            ));
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            //Image types
                            if(hfmine.equals("image/gif") || bitmap.getByteCount() > MAX_BITMAP_SIZE){
                                Glide.with(ll.getContext()).load(file).into(iv);
                            } else iv.setImageBitmap(bitmap);
                            ll.addView(iv, 0);
                            mQSContainerImpl.addView(ll, 1);
                        }
                        XposedBridge.log("Okay image "+file.getAbsolutePath());
                    } else XposedBridge.log("not found");
                }
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "onFinishInflate", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;
                View mStatusBarBackground = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground");
                int bh = Static.dpToPx(mStatusBarBackground.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                mStatusBarBackground.getLayoutParams().height = bh;

                LinearLayout ll = new LinearLayout(mQSContainerImpl.getContext());
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        bh
                ));
                ll.setOrientation(LinearLayout.HORIZONTAL);
                int ma = Static.dpToPx(mQSContainerImpl.getContext(), 4);
                ll.setPadding(ma, ma, ma, ma);

                String hfmine = mPrefs.getString("status_bar_custom_header_image_type", "unk");

                if(!hfmine.equals("unk")){

                    File file = new File(mPrefs.getFile().getParent() + "/custom_file_header_image");
                    if (file.exists()) {
                        if(hfmine.equals("video/mp4")){
                            mTextureView = new TextureView(ll.getContext());
                            mTextureView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            ));
                            ll.addView(mTextureView, 0);
                            mQSContainerImpl.addView(ll, 1);
                            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
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
                            if (mTextureView.isAvailable()) onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
                            try {
                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setVolume(0,0);
                                mMediaPlayer.setDataSource(file.getAbsolutePath());
                                mMediaPlayer.setLooping(true);

                                mMediaPlayer.prepare();

                                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        mediaPlayer.start();
                                    }
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mTextureView.post(()->{
                                calculateVideoSize(file);
                                updateTextureViewSize(ll.getWidth(), bh);
                            });
                        } else {
                            //Image
                            ImageView iv = new ImageView(ll.getContext());
                            iv.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            ));
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            //Image types
                            if(hfmine.equals("image/gif") || bitmap.getByteCount() > MAX_BITMAP_SIZE){
                                Glide.with(ll.getContext()).load(file).into(iv);
                            } else iv.setImageBitmap(bitmap);
                            ll.addView(iv, 0);
                            mQSContainerImpl.addView(ll, 1);
                        }
                        XposedBridge.log("Okay image "+file.getAbsolutePath());
                    } else XposedBridge.log("not found");
                }
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSFragment", classLoader, "onDestroy", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                if (mMediaPlayer != null) {
                    XposedBridge.log("stop");
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
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

    public void initInit(final XSharedPreferences xSharedPreferences, XResources res){
        mPrefs2 = xSharedPreferences;

        res.hookLayout("com.android.systemui", "layout", "qs_panel", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                View background = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_status_bar_background", "id", "com.android.systemui"));
                if(mPrefs2.getBoolean("qs_header_transparency", false)) background.setBackgroundColor(0x00000000);
                View gradient = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_gradient_view", "id", "com.android.systemui"));
                if(gradient != null) gradient.setVisibility(View.GONE);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_customize_panel_content", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs2.getInt("status_bar_custom_header_height", 25));

                View customizer_transparent_view = liparam.view.findViewById(liparam.res.getIdentifier("customizer_transparent_view", "id", "com.android.systemui"));
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) customizer_transparent_view.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_detail", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs2.getInt("status_bar_custom_header_height", 25));
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

        mTextureView.setTransform(matrix);
        mTextureView.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
    }
}
