package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Tools;
import net.servokio.vanilla.preferences.CustomSeekBarPreference;

public class ALockScreenUI extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_lock_screen_ui);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.lock_screen_ui, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
        private LinearLayout l = null;
        private boolean v = true;
        private ImageView imageView = null;
        private TextClock clockView = null;
        private float alpha = 0;

        @Override
        public void onResume(){
            super.onResume();
            MainActivity.getInstance().prefs.registerOnSharedPreferenceChangeListener(this);
            updateClock();
        }
        @Override
        public void onPause(){
            MainActivity.getInstance().prefs.unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_lock_screen_ui, rootKey);

            Preference pref = findPreference("lockscreen_widgets_list");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), WidgetsList.class));
                return true;
            });

            pref = findPreference("lockscreen_clock_position");
            if (pref != null) {
                DisplayMetrics metrics = this.getResources().getDisplayMetrics();
                ((CustomSeekBarPreference) pref).setMax(metrics.heightPixels);
            }

            pref = findPreference("lockscreen_colors");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), ALockScreenColors.class));
                return true;
            });

            pref = findPreference("lockscreen_click");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                play();
                return true;
            });
        }

        void play(){
            if(l == null) return;
            if(this.v){
                AlphaAnimation anim = new AlphaAnimation(alpha, 1);
                anim.setDuration(350);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {l.setAlpha(1);}
                    @Override
                    public void onAnimationEnd(Animation animation) {}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                zoomNorm();
                l.startAnimation(anim);
            } else {
                screenON();
            }
            this.v = !this.v;
        }

        void screenON(){
            AlphaAnimation anim = new AlphaAnimation(1,alpha);
            anim.setDuration(350);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {l.setAlpha(alpha);}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            zoomBig();
            l.startAnimation(anim);
        }

        void zoomNorm(){
            ScaleAnimation animation = new ScaleAnimation(1.1f, 1, 1.1f, 1, ScaleAnimation.RELATIVE_TO_SELF,(float)0.5,ScaleAnimation.RELATIVE_TO_SELF,(float)0.5);
            animation.setDuration(350);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {imageView.setScaleX(1);imageView.setScaleY(1);}
                @Override
                public void onAnimationEnd(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            imageView.startAnimation(animation);
        }

        void zoomBig(){
            ScaleAnimation animation = new ScaleAnimation(1, 1.1f, 1, 1.1f, ScaleAnimation.RELATIVE_TO_SELF,(float)0.5,ScaleAnimation.RELATIVE_TO_SELF,(float)0.5);
            animation.setDuration(350);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {imageView.setScaleX(1.1f);imageView.setScaleY(1.1f);}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            imageView.startAnimation(animation);
        }

        void updateClock(){
            if(clockView == null) return;
            clockView.setRotation(MainActivity.getInstance().prefs.getInt("lockscreen_clock_rotation", 0));
            if(MainActivity.getInstance().prefs.getBoolean("lockscreen_clock_color_enable", false)) clockView.setTextColor(MainActivity.getInstance().prefs.getInt("lockscreen_clock_color", 0xffffffff));
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            recyclerView.post(() -> {
                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.getInstance());
                if (Build.VERSION.SDK_INT >= 24 && ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    ParcelFileDescriptor pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
                    if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                    if (pfd != null) {
                        imageView = getListView().findViewById(R.id.imageView4);
                        clockView = getListView().findViewById(R.id.clock_view);
                        updateClock();
                        final Bitmap result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        imageView.setImageBitmap(result);
                        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.test);
                        imageView.startAnimation(animation);
                        l = getListView().findViewById(R.id.black);
                        alpha = (100 - MainActivity.getInstance().prefs.getInt("lockscreen_bg_opacity", 55)) / 100.0f;
                        screenON();
                    }
                }
            });
            return recyclerView;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            System.out.println(key);
            if (key.equals("lockscreen_bg_opacity") && l != null) {
                alpha = (100 - sharedPreferences.getInt("lockscreen_bg_opacity", 55)) / 100.0f;
                l.setAlpha(alpha);
            } else if (key.equals("lockscreen_wallpaper_zoom") && imageView != null) {
                if(sharedPreferences.getBoolean("lockscreen_wallpaper_zoom", true)){
                    zoomBig();
                } else zoomNorm();
            } else if (key.equals("lockscreen_clock_rotation") && clockView != null) {
                updateClock();
            }
        }
    }
}