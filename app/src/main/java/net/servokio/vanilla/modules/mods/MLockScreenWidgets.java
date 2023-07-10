package net.servokio.vanilla.modules.mods;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.XResources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.servokio.vanilla.modules.mods.lockScreenWidget.HostViewConfiguration;
import net.servokio.vanilla.modules.mods.lockScreenWidget.LSWidgetHostView;
import net.servokio.vanilla.modules.mods.lockScreenWidget.WidgetHost;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MLockScreenWidgets {
    private static XSharedPreferences mPrefs;

    private static ViewGroup keyguardStatusView = null;
    private static ViewGroup keyguardContainer = null;
    private static Context contextSsr = null;

    private static WidgetHost mAppWidgetHost = null;
    private static AppWidgetManager mAppWidgetManager = null;

    private static final int HOST_TD = 12345;
    private static int keyguardChildCount = -1;
    private static int containerChildCount = -1;
    private static boolean containerIsLinear = false;
    private static boolean widgetsRestored = false;

    public void initLoad(final XSharedPreferences xSharedPreferences, final ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

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
//                IntentFilter intentFilter = new IntentFilter();
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_ARE_YOU_ALIVE);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_CREATE_WIDGET);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_ADD_WIDGET);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_RECONFIGURE_WIDGET);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_DELETE_APPWIDGET_ID);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_DELETE_WIDGET);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_DELETE_ALL_WIDGETS);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_BONUS);
//                intentFilter.addAction(XposedLockScreenWidgets.ACTION_DOZE_IMPROVEMENTS);
//                intentFilter.addAction("android.intent.action.USER_PRESENT");
//                XposedLockScreenWidgets.contextSsr.registerReceiver(XposedLockScreenWidgets.mBroadcastReceiver, intentFilter);
//                if (XposedLockScreenWidgets.debugMode) {
//                    XposedBridge.log("LockScreenWidgets ==========>>> KeyguardStatusView BroadcastReceiver registered");
//                }
                AppWidgetManager unused3 = mAppWidgetManager = AppWidgetManager.getInstance(contextSsr);
                WidgetHost unused4 = mAppWidgetHost = new WidgetHost(contextSsr, HOST_TD);
            }
        });


        XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardStatusView", classLoader, "onFinishInflate", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (keyguardStatusView == null) {
                    ViewGroup unused = keyguardStatusView = (ViewGroup) param.thisObject;
                }
                int unused2 = keyguardChildCount = keyguardStatusView.getChildCount();
                try {
                    ViewGroup unused3 = keyguardContainer = (ViewGroup) keyguardStatusView.getChildAt(0);
                    int unused4 = containerChildCount = keyguardContainer.getChildCount();
                    boolean unused5 = containerIsLinear = keyguardContainer instanceof LinearLayout;
                } catch (ClassCastException e) {

                }
                if (Build.VERSION.SDK_INT < 24) {
                    restoreSavedWidgets();
                    //bonusOptions();
                } else {
                    boolean unused6 = widgetsRestored = false;
                }
                if (mPrefs.getBoolean("containerUseCustomGravity", false)) {
                    try {
                        keyguardStatusView.getLayoutParams().height = -1;
                        keyguardContainer.getLayoutParams().height = -1;
                        //((LinearLayout) keyguardContainer).setGravity(gravitiesConverterB[mPrefs.getInt("containerCustomGravity", 1)]);
                    } catch (Throwable t2) {
                        XposedBridge.log("<<<========== LockScreenWidgets containerUseCustomGravity: " + t2.toString());
                    }
                }
            }
        });
    }

    public static void restoreSavedWidgets() {
        if (keyguardContainer == null || contextSsr == null) {
            XposedBridge.log("<<<========== LockScreenWidgets restoreSavedWidgets. Precondition  failed...");
            return;
        }
//        String json = mPrefs.getString("WidgetConfigurationList", "");
//        Type type = new TypeToken<List<HostViewConfiguration>>() {}.getType();
//        List<HostViewConfiguration> widgetsConfigurationList = (List) new Gson().fromJson(json, type);
//        if (widgetsConfigurationList == null) widgetsConfigurationList = new ArrayList<>();
        //Collections.sort(widgetsConfigurationList, new WidgetConfigurationIndexComparator());
//        for (int i = widgetsConfigurationList.size() - 1; i >= 0; i--) {
//            HostViewConfiguration appWidgetConf = widgetsConfigurationList.get(i);
//            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetConf.getXposedId());
//            if (appWidgetInfo == null) XposedBridge.log("<<<========== LockScreenWidgets restoreSavedWidgets. No info found for XposedWidgetId: " + appWidgetConf.getXposedId());
//            LSWidgetHostView appWidgetHostView = (LSWidgetHostView) mAppWidgetHost.createView(contextSsr, appWidgetConf.getXposedId(), appWidgetInfo);
//            appWidgetHostView.updateHostViewWithConfiguration(appWidgetConf, appWidgetInfo, contextSsr);
//            int index = Math.min(appWidgetConf.getIndex(), keyguardContainer.getChildCount());
//            keyguardContainer.addView(appWidgetHostView, index);
//        }
//        widgetsConfigurationList.clear();

        //test
        HostViewConfiguration appWidgetConf = new HostViewConfiguration();
        //appWidgetConf.setXposedId();
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetConf.getXposedId());
        if (appWidgetInfo == null) XposedBridge.log("<<<========== LockScreenWidgets restoreSavedWidgets. No info found for XposedWidgetId: " + appWidgetConf.getXposedId());
        LSWidgetHostView appWidgetHostView = (LSWidgetHostView) mAppWidgetHost.createView(contextSsr, appWidgetConf.getXposedId(), appWidgetInfo);
        appWidgetHostView.updateHostViewWithConfiguration(appWidgetConf, appWidgetInfo, contextSsr);
        keyguardContainer.addView(appWidgetHostView);
    }
}
