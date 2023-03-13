package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.FontListParser;

import java.util.List;

public class ACarrierLabel extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_carrier_label);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.carrier_label, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_carrier_label, rootKey);

            ListPreference pref = findPreference("carrier_label_font_style");
            if(pref != null){
                List<FontListParser.SystemFont> fonts;
                try {
                    fonts = FontListParser.getSystemFonts();
                } catch (Exception e) {
                    pref.setDialogMessage("Sys error: "+e.getMessage());
                    fonts = FontListParser.safelyGetSystemFonts();
                }
                CharSequence[] entries = new String[fonts.size() + 1];
                entries[0] = "Normal (default)";
                CharSequence[] entryValues = new String[fonts.size() + 1];
                entryValues[0] = "";
                for (int i = 0; i < fonts.size(); i++) {
                    entries[i+1] = fonts.get(i).formated;
                    entryValues[i+1] = fonts.get(i).path;
                }
                pref.setEntries(entries);
                pref.setEntryValues(entryValues);
            }
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
                        ImageView imageView = getListView().findViewById(R.id.imageView4);
                        final Bitmap result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        imageView.setImageBitmap(result);
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
                }
            });
            return recyclerView;
        }
    }
}
