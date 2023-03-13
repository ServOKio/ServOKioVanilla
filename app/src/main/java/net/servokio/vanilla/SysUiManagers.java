package net.servokio.vanilla;

import android.content.Context;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class SysUiManagers {
    public static BroadcastMediator BroadcastMediator;

    public static void init() {
        BroadcastMediator = new BroadcastMediator();
        XposedBridge.log("Sys ui ok");
    }

    public static void initContext(Context context) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null");
        BroadcastMediator.setContext(context);
    }
}
