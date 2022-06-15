package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
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

public class ALockScreenUI extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_lock_screen_ui);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.lock_screen_ui, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_lock_screen_ui, rootKey);
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
                    if (Build.VERSION.SDK_INT >= 24){
                        ParcelFileDescriptor pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
                        if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                        if (pfd != null) {
                            final Bitmap result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                            try {
                                pfd.close();
                                ImageView imageView = phoneBlock.findViewById(R.id.imageView4);
                                imageView.setImageDrawable(new BitmapDrawable(getResources(), result));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
                phoneBlock.setAlpha(1);
                phoneBlock.startAnimation(animation);
            });
            return recyclerView;
        }
    }
}
