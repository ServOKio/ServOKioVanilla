package net.servokio.vanilla.modules;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.view.View;

import net.servokio.vanilla.BuildConfig;

import org.xmlpull.v1.XmlPullParser;

public class SafeModuleResources {
    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    private final XModuleResources mModuleRes;

    private SafeModuleResources(XModuleResources moduleRes) {
        mModuleRes = moduleRes;
    }

    public static SafeModuleResources createInstance(String path, XResources origRes) {
        XModuleResources moduleRes = XModuleResources.createInstance(path, origRes);
        return new SafeModuleResources(moduleRes);
    }

    private int getResId(String resName, String resType) {
        int resId = mModuleRes.getIdentifier(resName, resType, PACKAGE_NAME);
        if (resId == 0) {
            throw new Resources.NotFoundException("Could not find " + resType + ": " + resName);
        }
        return resId;
    }

    public String getString(String resName) {
        int resId = getResId(resName, "string");
        return mModuleRes.getString(resId);
    }

    public XmlPullParser getLayout(String resName) {
        int resId = getResId(resName, "layout");
        return mModuleRes.getLayout(resId);
    }

    public View findViewById(View view, String idName) {
        int id = getResId(idName, "id");
        return view.findViewById(id);
    }
}