package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;

import java.io.File;

public class AAboutDevice extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.holder_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.holder_main, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_about_device, rootKey);

            Preference pref = findPreference("android_version");
            if(pref != null){
                pref.setTitle(Static.currentVersion());
                pref.setSummary(Static.getSystemProperty("ro.system.build.fingerprint"));
            }

            pref = findPreference("ctx_dir");
            if(pref != null) pref.setSummary(getContext().getFilesDir().getAbsolutePath());

            pref = findPreference("files_dir");
            if(pref != null) pref.setSummary(new File(Static.getPreferenceDir(getActivity())).getAbsolutePath());

            pref = findPreference("build_manufacturer");
            if(pref != null) pref.setSummary(Build.MANUFACTURER);

            pref = findPreference("build_display");
            if(pref != null) pref.setSummary(Build.DISPLAY);

            pref = findPreference("build_hardware");
            if(pref != null) pref.setSummary(Build.HARDWARE);

            pref = findPreference("build_device");
            if(pref != null) pref.setSummary(Build.DEVICE);

            pref = findPreference("settings_accent_color");
            if(pref != null){
                try {
                    int intForUser = Settings.System.getInt(this.getContext().getContentResolver(), "accent_color");
                    pref.setSummary(String.format("#%08x", intForUser) + " ("+intForUser+")");
                    pref.getIcon().setTint(intForUser);
                } catch (Settings.SettingNotFoundException e) {
                    pref.setVisible(false);
                }
            }
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            recyclerView.post(() -> {
                ImageView imageView = getListView().findViewById(R.id.imageView4);
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.getInstance());
                if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                    imageView.setImageDrawable(wallpaperDrawable);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.test);
                    imageView.startAnimation(animation);
                    LinearLayout l = getListView().findViewById(R.id.black);
                    Animation opa = AnimationUtils.loadAnimation(getContext(), R.anim.opacity);
                    opa.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {l.setAlpha(0);}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    l.startAnimation(opa);
                }
            });
            return recyclerView;
        }
    }
}
