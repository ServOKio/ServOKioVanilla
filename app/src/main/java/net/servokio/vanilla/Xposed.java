package net.servokio.vanilla;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.content.Intent;
import android.content.res.XResources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.servokio.vanilla.modules.mods.MHeaderClock;
import net.servokio.vanilla.modules.mods.MHeaderImage;
import net.servokio.vanilla.modules.mods.MLockScreenWidgets;
import net.servokio.vanilla.modules.mods.MLockscreen;
import net.servokio.vanilla.ui.main.Intents;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xposed implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static String MODULE_PATH = null;

    private MHeaderImage mHeaderImage;
    private MLockScreenWidgets mLockScreenWidgets;
    private MHeaderClock mHeaderClock;
    private MLockscreen mLockscreen;

    private GridLayout mKeyguardStatusView;
    private Object mNotificationPanelViewController;
    private List<Integer> colorPalette = new ArrayList<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        this.mHeaderImage = new MHeaderImage();
        this.mLockScreenWidgets = new MLockScreenWidgets();
        this.mHeaderClock = new MHeaderClock();
        this.mLockscreen = new MLockscreen();
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        XResources res = resparam.res;
        XSharedPreferences prefs = getPrefs();
        if (resparam.packageName.equals(BuildConfig.APPLICATION_ID)){
            res.setReplacement(R.bool.xposed, true);
            res.setReplacement(R.integer.test, colorPalette.size());
            if(prefs.contains("accent_color")) {
                res.setReplacement(R.color.main_theme, prefs.getInt("accent_color", R.color.main_theme));
                res.setReplacement(BuildConfig.APPLICATION_ID, "color", "settings_header_icon", prefs.getInt("accent_color", R.color.settings_header_icon));
            }
        }

        if (resparam.packageName.equals("com.android.systemui")) {

            resparam.res.hookLayout("com.android.systemui", "layout", "keyguard_status_bar", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                    TextView keyguard_carrier_text = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_carrier_text", "id", "com.android.systemui"));
                    if(Arrays.asList("0","2").contains(prefs.getString("carrier_label", "3"))) {
                        keyguard_carrier_text.setVisibility(View.GONE);
                    } else {
                        String t = prefs.getString("carrier_label_text", "");
                        if(!t.equals("")) keyguard_carrier_text.setText(t);
                        int c = prefs.getInt("carrier_label_color", 0xffffffff);
                        keyguard_carrier_text.setTextColor(c);
                    }
                }
            });

            //resparam.res.setReplacement("com.android.systemui", "string", "quick_settings_bluetooth_label", "HOO!");

            if(prefs.getBoolean("status_bar_custom_header", false)) mHeaderImage.initInit(prefs, res);
            mLockScreenWidgets.initInit(prefs, res);
            mHeaderClock.initInit(prefs, res);
            mLockscreen.initInit(prefs, res);
        }
    }

    private static BroadcastMediator.Receiver mBroadcastReceiver = (context, intent) -> {
        String action = intent.getAction();
        if (action.equals(Intents.ACTION_TEST)){
            XposedBridge.log("Intent: TEST");
        }
//                || action.equals(GravityBoxSettings.ACTION_PREF_LOCKSCREEN_BG_CHANGED)) {
//            mPrefs.reload();
//            prepareCustomBackground(true);
//            prepareBottomActions();
//            if (DEBUG) log("Settings reloaded");
//        } else if (action.equals(KeyguardImageService.ACTION_KEYGUARD_IMAGE_UPDATED)) {
//            if (DEBUG_KIS) log("ACTION_KEYGUARD_IMAGE_UPDATED received");
//            setLastScreenBackground(true);
//        } else if (action.equals(QuietHoursActivity.ACTION_QUIET_HOURS_CHANGED)) {
//            mQuietHours = new QuietHours(intent.getExtras());
//            if (DEBUG) log("QuietHours settings reloaded");
//        } else if (action.equals(GravityBoxSettings.ACTION_PREF_LOCKSCREEN_SHORTCUT_CHANGED)) {
//            if (mAppBar != null) {
//                if (intent.hasExtra(GravityBoxSettings.EXTRA_LS_SHORTCUT_SLOT)) {
//                    mAppBar.updateAppSlot(intent.getIntExtra(GravityBoxSettings.EXTRA_LS_SHORTCUT_SLOT, 0),
//                            intent.getStringExtra(GravityBoxSettings.EXTRA_LS_SHORTCUT_VALUE));
//                }
//                if (intent.hasExtra(GravityBoxSettings.EXTRA_LS_SAFE_LAUNCH)) {
//                    mAppBar.setSafeLaunchEnabled(intent.getBooleanExtra(
//                            GravityBoxSettings.EXTRA_LS_SAFE_LAUNCH, false));
//                }
//                if (intent.hasExtra(GravityBoxSettings.EXTRA_LS_SHOW_BADGES)) {
//                    mAppBar.setShowBadges(intent.getBooleanExtra(
//                            GravityBoxSettings.EXTRA_LS_SHOW_BADGES, false));
//                }
//                if (intent.hasExtra(GravityBoxSettings.EXTRA_LS_SCALE)) {
//                    mAppBar.setScale(intent.getIntExtra(GravityBoxSettings.EXTRA_LS_SCALE, 0));
//                }
//            }
//        } else if (action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)
//                || action.equals(Intent.ACTION_USER_UNLOCKED)) {
//            if (mAppBar != null)
//                mAppBar.initAppSlots();
//            prepareBottomActions();
//        }
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.systemui")) return;
        XposedBridge.log("Init sys ui");
        SysUiManagers.init();
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.makeWorldReadable();
        if(prefs.getBoolean("status_bar_custom_header", false)) mHeaderImage.initLoad(prefs, lpparam.classLoader);
        mLockScreenWidgets.initLoad(prefs, lpparam.classLoader);
        mHeaderClock.initLoad(prefs, lpparam.classLoader);
        mLockscreen.initLoad(prefs, lpparam.classLoader);

        //CarrierText
        findAndHookMethod("com.android.keyguard.CarrierTextController", lpparam.classLoader, "postToCallback", "com.android.keyguard.CarrierTextController.CarrierTextCallbackInfo", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                String t = prefs.getString("carrier_label_text", "");
                if (!t.isEmpty()) {
                    Object obj = methodHookParam.args[0];
                    XposedHelpers.setObjectField(obj, "carrierText", t);
                }
            }
        });

        findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.classLoader, "updateIconsAndTextColors", new XC_MethodHook() {
            protected void afterHookedMethod (MethodHookParam methodHookParam) {
                TextView v = (TextView)XposedHelpers.getObjectField(methodHookParam.thisObject, "mCarrierLabel");
                if(prefs.contains("status_bar_carrier_label_font_size")) v.setTextSize(prefs.getInt("status_bar_carrier_label_font_size", 14));
                if(prefs.contains("carrier_label_color")) v.setTextColor(prefs.getInt("carrier_label_color", 0xffffffff));
                if(prefs.contains("carrier_label_font_style") && !prefs.getString("carrier_label_font_style", "").equals("")) v.setTypeface(Typeface.createFromFile(prefs.getString("carrier_label_font_style", "")));
            }
        });

        try {
            if(!prefs.getBoolean("lockscreen_wallpaper_zoom", true)) XposedBridge.hookMethod(XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader).getDeclaredMethod("lambda$makeStatusBarView$5", float.class, XposedHelpers.findClassIfExists("com.android.systemui.statusbar.BackDropView", lpparam.classLoader), float.class), XC_MethodReplacement.DO_NOTHING);
        } catch (NoSuchMethodException ignored) {}

//        try {
//            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelViewController", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
//                protected void afterHookedMethod(MethodHookParam methodHookParam) {
//                    ViewGroup test = (ViewGroup) methodHookParam.thisObject;
//                    test.setBackgroundColor(0xffff0000);
//                    test.setVisibility(View.GONE);
//                }
//            });
//        } catch (Throwable th) {
//            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
//        }
        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelViewController", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    Object t = mNotificationPanelViewController = methodHookParam.thisObject;
                    mKeyguardStatusView = (GridLayout) XposedHelpers.getObjectField(t, "mKeyguardStatusView");
                    //mKeyguardStatusView.setBackgroundColor(0xffff0000);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            if(prefs.getBoolean("lockscreen_clock_and_notifications_position", false)) XposedBridge.hookMethod(XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.NotificationPanelViewController", lpparam.classLoader).getDeclaredMethod("positionClockAndNotifications"), new XC_MethodReplacement(){
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    //mKeyguardStatusView.setBackgroundColor(0xff5500ff);
                    mKeyguardStatusView.setY(prefs.getInt("lockscreen_clock_position", 25));
                    //            int getUnlockedStackScrollerPadding = (int)XposedHelpers.callMethod(mNotificationPanelViewController, "getUnlockedStackScrollerPadding");
        //            boolean isAddOrRemoveAnimationPending = (boolean) XposedHelpers.callMethod(XposedHelpers.getObjectField(mNotificationPanelViewController, "mNotificationStackScroller"), "isAddOrRemoveAnimationPending");
        //
        //            XposedHelpers.callMethod(XposedHelpers.getObjectField(mNotificationPanelViewController, "mNotificationStackScroller"), "setIntrinsicPadding", getUnlockedStackScrollerPadding);
        //            int mStackScrollerMeasuringPass = XposedHelpers.getIntField(mNotificationPanelViewController, "mStackScrollerMeasuringPass");
        //            XposedHelpers.setObjectField(mNotificationPanelViewController, "mStackScrollerMeasuringPass", mStackScrollerMeasuringPass+1);
        //            XposedHelpers.callMethod(mNotificationPanelViewController, "requestScrollerTopPaddingUpdate", isAddOrRemoveAnimationPending);
        //            XposedHelpers.setObjectField(mNotificationPanelViewController, "mStackScrollerMeasuringPass", 0);
        //            XposedHelpers.setObjectField(mNotificationPanelViewController, "mAnimateNextPositionUpdate", false);
                    return null;
                }
            });
        } catch (NoSuchMethodException ignored) {
            XposedBridge.log(ignored);
        }

        XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.LockIcon", lpparam.classLoader, "updateIconVisibility", boolean.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ImageView t = (ImageView) methodHookParam.thisObject;
                if(!prefs.getBoolean("lockscreen_lock_icon", true)) t.setVisibility(View.GONE);
            }
        });


//        try {
//            findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelViewController.OnLayoutChangeListener", lpparam.classLoader, "onLayoutChange", View.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
//                protected void afterHookedMethod(MethodHookParam methodHookParam) {
//                    if(mKeyguardStatusView != null){
//                        mKeyguardStatusView.setBackgroundColor(0xffffff00);
//                        mKeyguardStatusView.setPivotY(0);
//                    }
//                }
//            });
//        } catch (Throwable th) {
//            XposedBridge.log(th);
//        }

        //broadcast
        try{
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "setupLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

                    SysUiManagers.BroadcastMediator.subscribe(mBroadcastReceiver,
                            Intents.ACTION_TEST
                    );
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            final Class<?> classSystemUIService = XposedHelpers.findClass("com.android.systemui.SystemUIService", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(classSystemUIService, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) {
                    Context context = (Context) param.thisObject;
                    try {
                        SysUiManagers.initContext(context);
                    } catch (Throwable t) {
                        XposedBridge.log("Error initializing SystemUI managers: "+ t.getMessage());
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking SystemUIService: "+th.getMessage());
        }
        //Hack colors
        try {
            XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardClockSwitch", lpparam.classLoader, "updateColors", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam methodHookParam) {
                    Object t = methodHookParam.thisObject;
                    int[] colors = (int[]) XposedHelpers.getObjectField(t, "mColorPalette");
                    for(int c : colors) colorPalette.add(c);
                    //XposedHelpers.setObjectField(obj, "carrierText", t);
                    //mKeyguardStatusView.setBackgroundColor(0xffff0000);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }
    }

    private XSharedPreferences getPrefs() {
        XSharedPreferences prefs;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || XposedBridge.getXposedVersion() > 92) {
            prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            prefs.makeWorldReadable();
        } else prefs = new XSharedPreferences(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BuildConfig.APPLICATION_ID + "_preferences.xml"));
        return prefs;
    }
}