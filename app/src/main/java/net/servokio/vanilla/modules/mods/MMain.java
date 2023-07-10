package net.servokio.vanilla.modules.mods;

import android.content.res.XResources;

import de.robv.android.xposed.XSharedPreferences;

public interface MMain {
    void initLoad(final XSharedPreferences xSharedPreferences, final ClassLoader classLoader);
    void initInit(XResources res);
}
