package net.servokio.vanilla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import net.servokio.vanilla.modules.Res;
import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.modules.Tools;
import net.servokio.vanilla.ui.main.SectionsPagerAdapter;
import net.servokio.vanilla.databinding.ActivityMainBinding;
import net.servokio.vanilla.ui.main.pages.AboutApp;
import net.servokio.vanilla.ui.main.sub.ALockScreenUI;

import java.io.File;

import de.robv.android.xposed.XposedBridge;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static MainActivity sInstance;
    public static SharedPreferences prefs;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sInstance = this;
        prefs = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", isXposedModuleEnabled() ? Context.MODE_WORLD_READABLE : Context.MODE_PRIVATE);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.sd_card:
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Memory access has already been granted", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 1);
                    }
                    return true;
                case R.id.xposed:
                    Toast.makeText(this, "The Xposed plugin is not activated", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });

        //External storage
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) findViewById(R.id.sd_card).setVisibility(View.GONE);

        //Xposed
        boolean xposedEnabled = isXposedModuleEnabled();
        if (xposedEnabled) {
            findViewById(R.id.xposed).setVisibility(View.GONE);
            try {
                int intForUser = Settings.System.getInt(getContentResolver(), "accent_color");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("accent_color", intForUser);
                editor.apply();
            } catch (Settings.SettingNotFoundException ignored) {}
        }

        FloatingActionButton fab = binding.restartSystemUi;
        fab.setOnClickListener(view -> Tools.rebootSystemUi());
        fab = binding.aboutVanilla;
        fab.setOnClickListener(view -> startActivity(new Intent(this, AboutApp.class)));
    }

    private boolean isXposedModuleEnabled() {
        return getResources().getBoolean(R.bool.xposed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static MainActivity getInstance() {
        return MainActivity.sInstance;
    }

}