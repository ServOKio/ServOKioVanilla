package net.servokio.vanilla;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.modules.mods.MHeaderImage;
import net.servokio.vanilla.ui.main.fixUI.HeaderImage;

import java.io.File;
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

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        this.mHeaderImage = new MHeaderImage();
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        XResources res = resparam.res;
        XSharedPreferences prefs = getPrefs();
        if (resparam.packageName.equals(BuildConfig.APPLICATION_ID)){
            res.setReplacement(R.bool.xposed, true);
            if(prefs.contains("accent_color")) {
                res.setReplacement(R.color.main_theme, prefs.getInt("accent_color", R.color.main_theme));
                res.setReplacement(BuildConfig.APPLICATION_ID, "color", "settings_header_icon", prefs.getInt("accent_color", R.color.settings_header_icon));
            }
        }

        if (resparam.packageName.equals("com.android.systemui")) {
            resparam.res.hookLayout("com.android.systemui", "layout", "keyguard_clock_switch", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) {
                    if(!prefs.getBoolean("lockscreen_clock", true)) liparam.view.findViewById(liparam.res.getIdentifier("keyguard_clock_container", "id", "com.android.systemui")).setVisibility(View.GONE);
                }
            });

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
            //Panel with header image


            //ok
//            resparam.res.hookLayout("com.android.systemui", "layout", "quick_status_bar_expanded_header", new XC_LayoutInflated() {
//                @Override
//                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
//                    ViewGroup mContainer = (ViewGroup) liparam.view;
//                    int bh = dpToPx(mContainer.getContext(), prefs.getInt("status_bar_custom_header_height", 25));
//                    mContainer.setBackgroundColor(0xff5500ff);
//                    mContainer.getLayoutParams().height = bh;
//                }
//            });

            //ok
            //там где часы - ХУЙНЯ
//            resparam.res.hookLayout("com.android.systemui", "layout", "quick_status_bar_header_system_icons", new XC_LayoutInflated() {
//                @Override
//                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
//                    FrameLayout mContainer = (FrameLayout) liparam.view;
//                    mContainer.setBackgroundColor(0xff00ff00);
//                    int bh = dpToPx(mContainer.getContext(), prefs.getInt("status_bar_custom_header_height", 25));
//                    mContainer.getLayoutParams().height = bh;
//                }
//            });

            //ok
//            resparam.res.hookLayout("com.android.systemui", "layout", "app_ops_info", new XC_LayoutInflated() {
//                @Override
//                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
//                    ViewGroup mContainer = (ViewGroup) liparam.view;
//                    int bh = dpToPx(mContainer.getContext(), prefs.getInt("status_bar_custom_header_height", 25));
//                    mContainer.getLayoutParams().height = bh;
//                }
//            });
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.systemui")) return;
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.makeWorldReadable();

        if(prefs.getBoolean("status_bar_custom_header", false)) mHeaderImage.initLoad(prefs, lpparam.classLoader);

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
                if(prefs.contains("carrier_label_color") && !prefs.getString("carrier_label_font_style", "").equals("")) v.setTypeface(Typeface.createFromFile(prefs.getString("carrier_label_font_style", "")));
            }
        });

        findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader, "scheduleUpdate", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                int i = prefs.getInt("lockscreen_bg_opacity", 55);
                float f = (100 - i) / 100.0f;
                for (Object obj : (Object[]) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.statusbar.phone.ScrimState", lpparam.classLoader), "values", new Object[0])) {
                    XposedHelpers.callMethod(obj, "setScrimBehindAlphaKeyguard", new Object[]{Float.valueOf(f)});
                }
            }
        });

        try {
            if(!prefs.getBoolean("lockscreen_wallpaper_zoom", true)) XposedBridge.hookMethod(XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader).getDeclaredMethod("lambda$makeStatusBarView$5", float.class, XposedHelpers.findClassIfExists("com.android.systemui.statusbar.BackDropView", lpparam.classLoader), float.class), XC_MethodReplacement.DO_NOTHING);
        } catch (NoSuchMethodException ignored) {}

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