package net.servokio.vanilla.modules;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;

public class Res extends Resources {

    public Res(Resources original) {
        super(original.getAssets(), original.getDisplayMetrics(), original.getConfiguration());
    }

    @Override public int getColor(int id) throws NotFoundException {
        return getColor(id, null);
    }

    @Override public int getColor(int id, Theme theme) throws NotFoundException {
        System.out.println(getResourceEntryName(id));
        switch (getResourceEntryName(id)) {
            case "main_theme":
                return Color.RED;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return super.getColor(id, theme);
                }
                return super.getColor(id);
        }
    }
}
