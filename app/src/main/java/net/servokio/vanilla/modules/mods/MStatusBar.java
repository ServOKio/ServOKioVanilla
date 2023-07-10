package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.servokio.vanilla.BuildConfig;
import net.servokio.vanilla.R;
import net.servokio.vanilla.Xposed;

import org.xmlpull.v1.XmlPullParser;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MStatusBar implements MMain{
    private XSharedPreferences mPrefs;

    XModuleResources modRes;

    //Icon controller
    private Object mService;
    private static Context mContext;

    private static int mFakeId;
    private static final String SLOT_WALKMAN_HOLD = "walkman_hold";

    @Override
    public void initLoad(XSharedPreferences xSharedPreferences, ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        //Naebalovo
        Class<?> classPhoneStatusBarPolicy = XposedHelpers.findClass("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", classLoader);
        XposedBridge.hookAllConstructors(classPhoneStatusBarPolicy, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mService = XposedHelpers.getObjectField(param.thisObject, "mIconController");
                mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                XposedBridge.log("1 ID: "+mFakeId);
                int d = mContext.getResources().getIdentifier("stat_sys_alarm", "drawable", "com.android.systemui");
                XposedBridge.log("2 ID: "+d);
//                setIcon(SLOT_WALKMAN_HOLD, mFakeId, 0, "The device is on holding. Use the HOLD switch to enable it.");
//                setIconVisibility(SLOT_WALKMAN_HOLD, true);
            }
        });

        XposedHelpers.findAndHookMethod(classPhoneStatusBarPolicy, "init", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
//                Object t = methodHookParam.thisObject;
//                mService = XposedHelpers.getObjectField(t, "mIconController");
//                mContext = (Context) XposedHelpers.getObjectField(t, "mContext");
                XposedBridge.log("1 ID: "+mFakeId);
                int d = mContext.getResources().getIdentifier("stat_sys_alarm", "drawable", "com.android.systemui");
                XposedBridge.log("2 ID: "+d);
                setIcon(SLOT_WALKMAN_HOLD, mFakeId, 0, "The device is on holding. Use the HOLD switch to enable it.");
                setIconVisibility(SLOT_WALKMAN_HOLD, mPrefs.getBoolean("statusbar_icons_walkman_hold_icon", false));
            }
        });


        findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", classLoader, "setIcon", String.class, int.class, CharSequence.class, new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                String one = (String) methodHookParam.args[0];
                int two = (int) methodHookParam.args[1];
                CharSequence three = (CharSequence) methodHookParam.args[2];

                XposedBridge.log("String: "+one);
                XposedBridge.log("int: "+two);
                XposedBridge.log("CharSequence: "+three);

                if(one.equals(SLOT_WALKMAN_HOLD)) {
                    int index = (int) XposedHelpers.callMethod(methodHookParam.thisObject, "getSlotIndex", SLOT_WALKMAN_HOLD);
                    XposedBridge.log("index: "+index);
                    Object icon = XposedHelpers.callMethod(methodHookParam.thisObject, "getIcon", index, 0);
                    XposedBridge.log("Is null: "+(icon == null ? "true" : "false"));
//                    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//                    for (StackTraceElement e : stackTraceElements) XposedBridge.log("      " + e.getClassName() + " | " + e.getMethodName() + " | " + e.getLineNumber());
                }
            }
        });
    }

    public void initLoadAndroid(ClassLoader classLoader){
        Class<?> classStatusBarManagerService = XposedHelpers.findClass("com.android.server.statusbar.StatusBarManagerService", classLoader);
        try{
            XposedHelpers.findAndHookMethod(classStatusBarManagerService, "getStatusBarIcons", String[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String[] slots = (String[]) param.args[0];
                    int N = slots.length;

                    String[] newSlots = new String[N + 1];
                    for (int i = 0; i < N; i++) {
                        newSlots[i] = slots[i];
                    }

                    newSlots[N-1] = SLOT_WALKMAN_HOLD;
                    param.args[0] = newSlots;
                }

                // 1 2 3
                // 0 1 2
                // 1 2 3 -
                // 0 1 2 3
            });
        } catch (Throwable th) {
            XposedBridge.log("Error hooking initLoadAndroid: "+th.getMessage());
        }
    }


    @Override
    public void initInit(XResources res) {
        modRes = XModuleResources.createInstance(Xposed.MODULE_PATH, res);
        mFakeId = res.addResource(modRes, R.drawable.walkman_stat_sys_hold);
    }

    public void setIcon(String slot, int iconId, int iconLevel, String contentDescription) {
        try {
            if (mService != null) {
                XposedBridge.log("call: ");
                XposedBridge.log("Class: "+mService.getClass());
                XposedBridge.log("Method: setIcon");
                XposedBridge.log("Slot: "+slot);
                XposedBridge.log("iconId: "+iconId);
                XposedBridge.log("contentDescription: "+contentDescription);
                XposedHelpers.callMethod(mService, "setIcon", slot, iconId, contentDescription);
            }
        } catch (Throwable ex) {
            // system process is dead anyway.
            throw new RuntimeException(ex);
        }
    }

    public void setIconVisibility(String slot, boolean visible) {
        try {
            if (mService != null) {
                XposedHelpers.callMethod(mService, "setIconVisibility", slot, visible);
            }
        } catch (Throwable ex) {
            // system process is dead anyway.
            throw new RuntimeException(ex);
        }
    }

    public void updateHoldState(boolean state) {
        setIcon(SLOT_WALKMAN_HOLD, mFakeId, 0, "The device is on holding. Use the HOLD switch to enable it.");
        setIconVisibility(SLOT_WALKMAN_HOLD, state);
    }

}
