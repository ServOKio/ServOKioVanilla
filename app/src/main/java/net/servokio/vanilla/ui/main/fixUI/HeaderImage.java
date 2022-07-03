package net.servokio.vanilla.ui.main.fixUI;

import android.content.Context;
import android.widget.LinearLayout;

import net.servokio.vanilla.R;

import de.robv.android.xposed.XposedBridge;

public class HeaderImage extends LinearLayout {

    public HeaderImage(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        XposedBridge.log("LID: "+R.layout.just_a_test);
        inflate(context, R.layout.just_a_test,this);
    }
}
