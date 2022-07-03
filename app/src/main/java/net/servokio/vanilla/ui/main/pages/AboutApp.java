package net.servokio.vanilla.ui.main.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.R;

public class AboutApp extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.about_app, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager preferenceManager = getPreferenceManager();
            setPreferencesFromResource(R.xml.about_app, rootKey);

//            Preference pref = findPreference("about_device");
//            if (pref != null) pref.setOnPreferenceClickListener(e -> {
//                startActivity(new Intent(getContext(), AAboutDevice.class));
//                return true;
//            });
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);

//            recyclerView.post(() -> {
//                View phoneBlock = getListView().findViewById(R.id.phone_animate);
//                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.getInstance());
//                if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    phoneBlock.findViewById(R.id.imageView4).setVisibility(View.GONE);
//                } else {
//                    final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
//                    ImageView imageView = phoneBlock.findViewById(R.id.imageView4);
//                    imageView.setImageDrawable(wallpaperDrawable);
//                }
//                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
//                phoneBlock.setAlpha(1);
//                phoneBlock.startAnimation(animation);
//            });
            return recyclerView;
        }
    }
}
