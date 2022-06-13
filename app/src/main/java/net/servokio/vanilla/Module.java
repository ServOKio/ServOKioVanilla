package net.servokio.vanilla;

import android.view.View;
import android.widget.FrameLayout;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class Module implements IXposedHookInitPackageResources {
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.android.systemui")) return;
        resparam.res.hookLayout("com.android.systemui", "layout", "keyguard_clock_switch", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) {
                ((FrameLayout) liparam.view.findViewById(liparam.res.getIdentifier("clock_view", "id", "com.android.systemui"))).setVisibility(View.GONE);
            }
        });
    }
}
