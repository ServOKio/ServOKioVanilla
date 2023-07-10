package net.servokio.vanilla.modules.mods;

import android.content.res.XResources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MHeaderClock implements MMain{
    private XSharedPreferences mPrefs;

    private ViewGroup mQsHeader;
    private ViewGroup mSystemIcons;
    private ViewGroup mPhoneStatusBar;
    private LinearLayout mStatusBarLeftSide;

    TextView clock;

    @Override
    public void initLoad(XSharedPreferences xSharedPreferences, ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.qs.QuickStatusBarHeader", classLoader, "onFinishInflate", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    mQsHeader = (ViewGroup) methodHookParam.thisObject;
                    int identifier = mQsHeader.getResources().getIdentifier("quick_status_bar_system_icons", "id", "com.android.systemui");
                    if (identifier != 0) {
                        mSystemIcons = mQsHeader.findViewById(identifier);
                        int identifier2 = mSystemIcons.getResources().getIdentifier("clock", "id", "com.android.systemui");
                        clock = mSystemIcons.findViewById(identifier2);
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", classLoader, "onFinishInflate", new XC_MethodHook() {
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) {
                    mPhoneStatusBar = (ViewGroup) methodHookParam.thisObject;
                    int identifier = mPhoneStatusBar.getResources().getIdentifier("status_bar_left_side", "id", "com.android.systemui");
                    if (identifier != 0) mStatusBarLeftSide = mPhoneStatusBar.findViewById(identifier);
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking onFinishInflate: "+th.getMessage());
        }

        try{
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", classLoader, "updateClock", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    TextView tv = (TextView) param.thisObject;
                    if(((View)tv.getParent()).getId() != mStatusBarLeftSide.getId()){
                        if(!mPrefs.getBoolean("show_qs_clock", true)){
                            tv.setVisibility(View.GONE);
                        } else {
                            if(tv.getVisibility() == View.VISIBLE){
                                if(mPrefs.contains("qs_header_clock_size")) tv.setTextSize(mPrefs.getInt("qs_header_clock_size", 14));
                                if(mPrefs.contains("qs_header_clock_color")) tv.setTextColor(mPrefs.getInt("qs_header_clock_color", 0xffffffff));
                                if(mPrefs.contains("qs_header_clock_font_style") && !mPrefs.getString("qs_header_clock_font_style", "").equals("")) tv.setTypeface(Typeface.createFromFile(mPrefs.getString("qs_header_clock_font_style", "")));
                            }
                        }
                    }
                }
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking updateClock: "+th.getMessage());
        }
    }

    @Override
    public void initInit(XResources res) {

    }
}
