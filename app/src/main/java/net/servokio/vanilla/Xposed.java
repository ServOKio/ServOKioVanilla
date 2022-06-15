package net.servokio.vanilla;

import android.content.res.XResources;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import java.io.File;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xposed implements IXposedHookInitPackageResources, IXposedHookLoadPackage {
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        XResources res = resparam.res;
        if (resparam.packageName.equals(BuildConfig.APPLICATION_ID)) res.setReplacement(R.bool.xposed, true);

        XSharedPreferences prefs = getPrefs();

        if (resparam.packageName.equals("com.android.systemui")) {
            resparam.res.hookLayout("com.android.systemui", "layout", "keyguard_clock_switch", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if(!prefs.getBoolean("lockscreen_clock", true)) liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setVisibility(View.GONE);
                }
            });
        }
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.systemui")) return;
//        findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                TextView tv = (TextView) param.thisObject;
//                tv.setText();
//                tv.setTextColor(Color.RED);
//            }
//        });
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
