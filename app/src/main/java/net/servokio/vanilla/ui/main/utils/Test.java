package net.servokio.vanilla.ui.main.utils;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.preferences.CustomSeekBarPreference;
import net.servokio.vanilla.ui.main.Intents;
import net.servokio.vanilla.ui.main.sub.ALockScreenColors;
import net.servokio.vanilla.ui.main.sub.ALockScreenUI;

public class Test extends AppCompatActivity {
    private boolean t = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        if (savedInstanceState == null) {
            Button button = (Button) findViewById(R.id.button);
            button.setText("Co: "+R.integer.test);
            button.setOnClickListener(v -> {
                t = !t;
                ((TextView) findViewById(R.id.textView)).setTextColor(t ? Color.RED : Color.GREEN);
                final Intent intent = new Intent();
                intent.setAction(Intents.ACTION_TEST);
                sendBroadcast(intent);
            });
        }

        for(int co : getResources().getIntArray(R.array.color_palette)){
            LinearLayout l = (LinearLayout)findViewById(R.id.list);
            Button b = new Button(this);
            b.setText("C: "+co);
            b.setBackgroundColor(co);
            l.addView(b);
        }
    }
}
