package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.UserManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import net.servokio.vanilla.BroadcastMediator;
import net.servokio.vanilla.SysUiManagers;
import net.servokio.vanilla.Xposed;
import net.servokio.vanilla.modules.SafeModuleResources;
import net.servokio.vanilla.ui.main.utils.MUtils;
import net.servokio.vanilla.ui.main.utils.Utils;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class MLockscreen implements MMain{
    private XSharedPreferences mPrefs;


    private static Context contextSsr = null;
    private static ViewGroup keyguardStatusView = null;

    //Owner info
    private TextView mOwnerInfo = null;


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

    private Context mContext;

    //Profile
    private static SafeModuleResources sModuleRes;

    private View mProfileRoot = null;
    private ImageView mProfileImageView;
    private TextView mProfileTextView;
    private static final String RES_ID_PROFILE_IMAGEVIEW = "profile_imageview";
    private static final String RES_ID_PROFILE_TEXTVIEW = "profile_textview";

    @Override
    public void initLoad(XSharedPreferences xSharedPreferences, ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        //gg
        try {
            XposedHelpers.findAndHookConstructor("com.android.keyguard.KeyguardStatusView", classLoader, Context.class, AttributeSet.class, Integer.TYPE, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Context unused = contextSsr = (Context) param.args[0];
                        ViewGroup unused2 = keyguardStatusView = (ViewGroup) param.thisObject;
                    } catch (ClassCastException e) {
                    }
                    if (contextSsr == null) {
                        XposedBridge.log("<<<========== LockScreenWidgets KeyguardStatusView Constructor contextSsr == null");
                        return;
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try{
            XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardStatusView", classLoader, "onFinishInflate", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mOwnerInfo = (TextView) XposedHelpers.getObjectField(param.thisObject, "mOwnerInfo");
                    if(mPrefs.contains("lockowner_text_color")) mOwnerInfo.setTextColor(mPrefs.getInt("lockowner_text_color", 0xffffffff));
                    mOwnerInfo.setShadowLayer(14, 0,0, Color.parseColor("#0000005d"));

                    //Profile icon
                    GridLayout self = (GridLayout)param.thisObject;
                    Context context = self.getContext();
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    XmlPullParser parser;
                    try {
                        parser = sModuleRes.getLayout("import_lockscreen_profile");
                    } catch (Resources.NotFoundException e) {
                        return;
                    }
                    mProfileRoot = layoutInflater.inflate(parser, null);
                    self.addView(mProfileRoot);
                    try {
                        mProfileImageView = (ImageView) sModuleRes.findViewById(mProfileRoot, RES_ID_PROFILE_IMAGEVIEW);
                        mProfileTextView = (TextView)sModuleRes.findViewById(mProfileRoot, RES_ID_PROFILE_TEXTVIEW);
                    } catch (Resources.NotFoundException e) {
                        return;
                    }

                    refreshUserProfile();
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

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

        // and when unlocked


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
                            MLockscreen.this.onSurfaceTextureAvailable(surfaceTexture, i, i1);
//                            if(!b){
//                                b = true;
//                                MLockscreen.this.onSurfaceTextureAvailable(surfaceTexture, i, i1);
//                            }
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

                    //debug
                    if(mPrefs.getBoolean("lockscreen_video_background", false)) mTextureView.post(()->{
                        ViewTreeObserver viewTreeObserver = mTextureView.getViewTreeObserver();
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    mTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    File file = new File(mPrefs.getFile().getParent() + "/custom_lockscreen_video");
                                    calculateVideoSize(file);
                                    updateTextureViewSize((int) mTextureView.getWidth(), (int) mTextureView.getHeight());
                                }
                            });
                        }
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

    private static void lambda$static$0(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.LOCKED_BOOT_COMPLETED") || action.equals("android.intent.action.USER_UNLOCKED")) {

        }
    }


//    private void playLockScreenVideo(){
//        String lcvmine = mPrefs.getString("lockscreen_custom_video_type", "unk");
//        if(!lcvmine.equals("unk")) {
//            File file = new File(mPrefs.getFile().getParent() + "/custom_lockscreen_video");
//
//            if(file.exists()){
//                try {
//                    if(mMediaPlayer != null){
//                        mMediaPlayer.pause();
//                        mMediaPlayer.release();
//                    }
//                    mMediaPlayer = new MediaPlayer();
//
//                    //mMediaPlayer.setVolume(0,0);
//                    mMediaPlayer.setDataSource(file.getAbsolutePath());
//                    mMediaPlayer.setLooping(true);
//
//                    mMediaPlayer.prepare();
//
//                    mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
//                        mediaReady = true;
//                        mediaPlayer.start();
//                        calculateVideoSize(file);
//                        updateTextureViewSize(backDropView.getWidth(), backDropView.getHeight());
//                    });
//                } catch (IOException e) {
//                    XposedBridge.log("Error playLockScreenVideo: "+e.getMessage());
//                }
//            } else XposedBridge.log("Error playLockScreenVideo file.exists(): "+file.exists());
//        } else XposedBridge.log("Error playLockScreenVideo lcvmine: "+lcvmine);
//    }

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
        Surface surface = new Surface(surfaceTexture);
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        System.out.println("New surface");
        try {
            File file = new File(mPrefs.getFile().getParent() + "/custom_lockscreen_video");
            mMediaPlayer = new MediaPlayer();
            //mMediaPlayer.setVolume(0,0);
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);

            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaReady = true;
                    mediaPlayer.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initInit(XResources res) {
        res.hookLayout("com.android.systemui", "layout", "keyguard_clock_switch", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                if(!mPrefs.getBoolean("lockscreen_clock", true)) {
                    liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setVisibility(View.GONE);
                } else {
                    liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setRotation(mPrefs.getInt("lockscreen_clock_rotation", 0));
                }

            }
        });

        res.hookLayout("com.android.systemui", "layout", "super_notification_shade", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                backDropView = liparam.view.findViewById(liparam.res.getIdentifier("backdrop", "id", "com.android.systemui"));
            }
        });

        sModuleRes = SafeModuleResources.createInstance(Xposed.MODULE_PATH, res);
    }

    private void calculateVideoSize(File file) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(file.getAbsolutePath());
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mVideoHeight = Float.parseFloat(height);
            mVideoWidth = Float.parseFloat(width);
            XposedBridge.log("calculateVideoSize X: "+mVideoWidth+" Y:"+mVideoHeight);
        } catch (NumberFormatException e) {
            XposedBridge.log("Error MHeaderImage: "+e.getMessage());
        }
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        XposedBridge.log("updateTextureViewSize X: "+viewWidth+" Y:"+viewHeight);
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

    private void refreshUserProfile(){
        String text = mPrefs.getString("lockscreen_profile_icon_username", null);
        File file = new File(mPrefs.getFile().getParent() + "/lockscreen_profile_icon_pic");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            mProfileImageView.setImageBitmap(MUtils.getRoundedCroppedBitmap(bitmap));
            mProfileTextView.setText(text);
        }

        mProfileRoot.setVisibility(mPrefs.getBoolean("lockscreen_profile_icon", false) ? View.VISIBLE : View.GONE);
    }

    public void onUnlocked() {
        videoPlay(false);
    }
}
