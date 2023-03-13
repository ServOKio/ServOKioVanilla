package net.servokio.vanilla.ui.main.pages;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.ui.main.sub.AAboutDevice;
import net.servokio.vanilla.ui.main.sub.BLockscreenPreview;
import net.servokio.vanilla.ui.main.utils.WallpaperEditor;

public class Miscellaneous extends Fragment {
    private FragmentManager fm;

    public Miscellaneous(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
//            cr(0);
        } else {
            System.out.println("not null");
        }
    }

    private void cr(int sender){
        fm.beginTransaction().replace(R.id.settings_miscellaneous, new SettingsFragment()).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.miscellaneous, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cr(1);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager preferenceManager = getPreferenceManager();
            setPreferencesFromResource(R.xml.settings_miscellaneous, rootKey);

            Preference pref = findPreference("about_device");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), AAboutDevice.class));
                return true;
            });

            pref = findPreference("wallpaper_editor");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), WallpaperEditor.class));
                return true;
            });
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