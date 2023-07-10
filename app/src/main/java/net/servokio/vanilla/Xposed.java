package net.servokio.vanilla;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.content.Intent;
import android.content.res.XResources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.servokio.vanilla.modules.mods.MHeaderClock;
import net.servokio.vanilla.modules.mods.MHeaderImage;
import net.servokio.vanilla.modules.mods.MLockScreenWidgets;
import net.servokio.vanilla.modules.mods.MLockscreen;
import net.servokio.vanilla.modules.mods.MStatusBar;
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
    public static String MODULE_PATH = null;

    private static MHeaderImage mHeaderImage;
    private MLockScreenWidgets mLockScreenWidgets;
    private MHeaderClock mHeaderClock;
    private static MLockscreen mLockscreen;
    private static MStatusBar mStatusBar;

    private GridLayout mKeyguardStatusView;
    private Object mNotificationPanelViewController;
    private List<Integer> colorPalette = new ArrayList<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mHeaderImage = new MHeaderImage();
        this.mLockScreenWidgets = new MLockScreenWidgets();
        this.mHeaderClock = new MHeaderClock();
        mLockscreen = new MLockscreen();
        mStatusBar = new MStatusBar();
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        XResources res = resparam.res;
        XSharedPreferences prefs = getPrefs();
        if (resparam.packageName.equals(BuildConfig.APPLICATION_ID)){
            res.setReplacement(R.bool.xposed, true);
            res.setReplacement(R.integer.test, colorPalette.size());

            // Сохранение системного цвета в настройки
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

                    // Текст оператора
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

            //if(prefs.getBoolean("status_bar_custom_header", false))
            mHeaderImage.initInit(res);
            mHeaderClock.initInit(res);
            mLockscreen.initInit(res);
            mStatusBar.initInit(res);
        }
    }

    //Тут броадкаст
    public static BroadcastMediator.Receiver mBroadcastReceiver = (context, intent) -> {
        String action = intent.getAction();
        XposedBridge.log("Vanilla: new itnent "+action);
        if (action.equals(Intents.ACTION_UPDATE_HEADER_IMAGE_HEIGHT)){
            XposedBridge.log("Vanilla: got ACTION_UPDATE_HEADER_IMAGE_HEIGHT");
            mHeaderImage.updateHeaderHeights();
        } else if (action.equals(Intents.ACTION_UPDATE_HEADER_IMAGE)){
            mHeaderImage.updateHeaderImage();
        } else if (action.equals(Intents.ACTION_UPDATE_HEADER_IMAGE_PREFS)){
            mHeaderImage.updatePrefs();
        } else if (action.equals(Intents.ACTION_UPDATE_STATUSBAR_ICONS_WALKMAN_HOLD)){
            mStatusBar.updateHoldState(intent.getBooleanExtra("value", false));
        }
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("com.android.systemui")){
            hookSysUI(lpparam);
        } else if (lpparam.packageName.equals("android")){
            hookAndroid(lpparam);
        }
    }

    private void hookSysUI(XC_LoadPackage.LoadPackageParam lpparam){
        SysUiManagers.init();
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.makeWorldReadable();

        mHeaderImage.initLoad(prefs, lpparam.classLoader);
        mLockScreenWidgets.initLoad(prefs, lpparam.classLoader);
        mHeaderClock.initLoad(prefs, lpparam.classLoader);
        mLockscreen.initLoad(prefs, lpparam.classLoader);
        mStatusBar.initLoad(prefs, lpparam.classLoader);

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

        //broadcast
        try{
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "setupLocked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

                    SysUiManagers.BroadcastMediator.subscribe(mBroadcastReceiver,
                            Intents.ACTION_UPDATE_HEADER_IMAGE,
                            Intents.ACTION_UPDATE_HEADER_IMAGE_HEIGHT,
                            Intents.ACTION_UPDATE_HEADER_IMAGE_PREFS,

                            Intents.ACTION_UPDATE_STATUSBAR_ICONS_WALKMAN_HOLD
                    );
                    XposedBridge.log("Vanilla: Now listening");
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
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }
    }

    private void hookAndroid(XC_LoadPackage.LoadPackageParam lpparam){
        mStatusBar.initLoadAndroid(lpparam.classLoader);
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