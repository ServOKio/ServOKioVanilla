package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class MLockscreen implements MMain{
    private XSharedPreferences mPrefs;
    private XSharedPreferences mPrefs2;

    private ImageView lockscreenWallpaper;
    private ImageView lockscreenWallpaperFront;

    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;

    private float mVideoWidth;
    private float mVideoHeight;

    private FrameLayout lockscreen;
    private FrameLayout backDropView;

    boolean b = false;

    boolean hideImagesOnLS = false;
    boolean screenOn = false;

    boolean videoProxyReady = false;
    boolean mediaReady = false;

    @Override
    public void initLoad(XSharedPreferences xSharedPreferences, ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        //хакаем лоскрин
        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", classLoader, "inflateStatusBarWindow", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    lockscreen = (FrameLayout) XposedHelpers.getObjectField(methodHookParam.thisObject, "mNotificationShadeWindowView");
                    //lockscreen.addView(mTextureView, 0);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", classLoader, "dispatchScreenTurnedOn", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    screenOn = true;
                    videoPlay(screenOn);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", classLoader, "dispatchScreenTurnedOff", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    screenOn = false;
                    videoPlay(screenOn);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarWindowController", classLoader, "attach", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    mTextureView = new TextureView(backDropView.getContext());
                    mTextureView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    ));
                    backDropView.addView(mTextureView, backDropView.getChildCount()-1);
                    mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                            if(!b){
                                b = true;
                                MLockscreen.this.onSurfaceTextureAvailable(surfaceTexture, i, i1);
                            }
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

                    lockscreenWallpaper.post(()->{
                        videoProxyReady = true;
                        playLockScreenVideo();
                    });
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", classLoader, "finishUpdateMediaMetaData", boolean.class, boolean.class, Bitmap.class, new XC_MethodHook() { // from class: com.ceco.r.gravitybox.ModLockscreen.2
                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    ImageView imageView = (ImageView) XposedHelpers.getObjectField(methodHookParam.thisObject, "mBackdropBack");
//                    if (ModLockscreen.mPrefs.getBoolean("pref_lockscreen_media_art_disable", false)) {
//                        methodHookParam.args[2] = null;
//                    }
                }

                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    lockscreenWallpaper = (ImageView) XposedHelpers.getObjectField(methodHookParam.thisObject, "mBackdropBack");
                    lockscreenWallpaperFront = (ImageView) XposedHelpers.getObjectField(methodHookParam.thisObject, "mBackdropFront");
                    if (backDropView != null && lockscreenWallpaper != null) {
                        boolean z = methodHookParam.args[2] != null;
                        int intValue = (Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(methodHookParam.thisObject, "mStatusBarStateController"), "getState", new Object[0]);
                        if(!z && intValue != 0){
                            Object a = XposedHelpers.getObjectField(methodHookParam.thisObject, "mLockscreenWallpaper");
                            Bitmap lockWallpaper = null;
                            if(a != null) lockWallpaper = (Bitmap) XposedHelpers.callMethod(a, "getBitmap");
                            if (lockWallpaper != null) {
                                lockscreenWallpaper.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                backDropView.animate().cancel();
                                lockscreenWallpaper.animate().cancel();
                                if(hideImagesOnLS){
                                    lockscreenWallpaper.setAlpha(0.5f);
                                    lockscreenWallpaperFront.setAlpha(0.5f);
                                    return;
                                }
                                lockscreenWallpaper.setImageBitmap(lockWallpaper);
                                backDropView.setVisibility(View.VISIBLE);
                                backDropView.animate().alpha(1.0f);
                            }
                        }
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try{
            findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", classLoader, "scheduleUpdate", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    int i = mPrefs.getInt("lockscreen_bg_opacity", 55);
                    float f = (100 - i) / 100.0f;
                    for (Object obj : (Object[]) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.statusbar.phone.ScrimState", classLoader), "values", new Object[0])) {
                        XposedHelpers.callMethod(obj, "setScrimBehindAlphaKeyguard", new Object[]{f});
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        //Clock color
        try {
            XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardClockSwitch", classLoader, "onFinishInflate", new XC_MethodHook() { // from class: com.ceco.r.gravitybox.ModLockscreen.2
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    TextClock clock = (TextClock) XposedHelpers.getObjectField(methodHookParam.thisObject, "mClockView");
                    if(mPrefs.getBoolean("lockscreen_clock_color_enable", false)) clock.setTextColor(mPrefs.getInt("lockscreen_clock_color", 0xffffffff));
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }
    }

    private void playLockScreenVideo(){
        String lcvmine = mPrefs.getString("lockscreen_custom_video_type", "unk");
        if(!lcvmine.equals("unk")) {
            File file = new File(mPrefs.getFile().getParent() + "/custom_lockscreen_video");

            if(file.exists()){
                try {
                    if(mMediaPlayer != null){
                        mMediaPlayer.pause();
                        mMediaPlayer.release();
                    }
                    mMediaPlayer = new MediaPlayer();

                    mMediaPlayer.setVolume(0,0);
                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                    mMediaPlayer.setLooping(true);

                    mMediaPlayer.prepare();

                    mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
                        mediaReady = true;
                        mediaPlayer.start();
                        calculateVideoSize(file);
                        updateTextureViewSize(backDropView.getWidth(), backDropView.getHeight());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void videoPlay(boolean state){
        if(!state){
            if(mediaReady && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        } else {
            if(mediaReady && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
            }
        }
    }

    private void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if(!mediaReady) return;
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void initInit(XSharedPreferences xSharedPreferences, XResources res) {
        mPrefs2 = xSharedPreferences;

        res.hookLayout("com.android.systemui", "layout", "keyguard_clock_switch", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                if(!mPrefs2.getBoolean("lockscreen_clock", true)) {
                    liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setVisibility(View.GONE);
                } else {
                    liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setRotation(mPrefs2.getInt("lockscreen_clock_rotation", 0));
                }

            }
        });

        res.hookLayout("com.android.systemui", "layout", "super_notification_shade", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                backDropView = liparam.view.findViewById(liparam.res.getIdentifier("backdrop", "id", "com.android.systemui"));
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
        mTextureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }
}
