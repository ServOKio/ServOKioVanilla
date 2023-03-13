package net.servokio.vanilla.ui.main;

import android.os.Build;

import java.util.HashMap;
import java.util.Locale;

public class PhoneIcons {
    public static HashMap<String, Integer> f = new HashMap<>();
    public static int get(){
        f.get((Build.MANUFACTURER+'-'+Build.DEVICE).toLowerCase(Locale.ROOT));
        return 1;
    }
}
