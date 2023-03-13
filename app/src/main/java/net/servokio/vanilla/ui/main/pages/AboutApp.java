package net.servokio.vanilla.ui.main.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.BuildConfig;
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

            Preference pref = findPreference("app_version");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                pref.setSummary(BuildConfig.VERSION_NAME);
                return true;
            });
        }
    }
}
