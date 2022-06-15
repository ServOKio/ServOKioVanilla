package net.servokio.vanilla.ui.main.pages;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.ui.main.sub.ALockScreenUI;

public class LockScreen extends Fragment {
    private FragmentManager fm;

    public LockScreen(FragmentManager fm) {
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
        fm.beginTransaction().replace(R.id.settings_lock_screen, new SettingsFragment()).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lock_screen, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cr(1);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_lockscreen, rootKey);

            Preference pref = findPreference("lock_screen_ui");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), ALockScreenUI.class));
                return true;
            });
        }

//        @MainThread
//        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
//            super.onViewCreated(view, savedInstanceState);
//            System.out.println("2");
//        }
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            System.out.println("7");
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            System.out.println("6");
//        }
//
//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            View v = super.onCreateView(inflater, container, savedInstanceState);
//            System.out.println("9");
//            return v;
//        }

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