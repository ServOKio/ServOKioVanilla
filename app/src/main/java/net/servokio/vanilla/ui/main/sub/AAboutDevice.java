package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;

public class AAboutDevice extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_about_device);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.about_phone, new SettingsFragment()).commit();
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
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            recyclerView.post(() -> {
                View phoneBlock = getListView().findViewById(R.id.phone_animate);
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.getInstance());
                if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    phoneBlock.findViewById(R.id.imageView4).setVisibility(View.GONE);
                } else {
                    final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                    ImageView imageView = phoneBlock.findViewById(R.id.imageView4);
                    imageView.setImageDrawable(wallpaperDrawable);
                }
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
                phoneBlock.setAlpha(1);
                phoneBlock.startAnimation(animation);
            });
            return recyclerView;
        }
    }
}
