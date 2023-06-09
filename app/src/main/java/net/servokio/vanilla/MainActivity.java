package net.servokio.vanilla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import net.servokio.vanilla.databinding.ActivityMainBinding;
import net.servokio.vanilla.modules.Tools;
import net.servokio.vanilla.ui.main.SectionsPagerAdapter;
import net.servokio.vanilla.ui.main.pages.AboutApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static MainActivity sInstance;
    public static SharedPreferences prefs;

    @SuppressLint({"NonConstantResourceId", "WorldReadableFiles"})
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
                        new MaterialAlertDialogBuilder(this).setTitle("Storage permissions").setMessage("For:\n● Wallpaper preview").setIcon(R.drawable.ic_sd_card).setPositiveButton("Let's enable", (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            }, 1);
                        }).setNeutralButton("Report bug", (dialogInterface, i) -> {
                            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ServOKio/vanilla")));
                        }).setNegativeButton("Ignore", (DialogInterface.OnClickListener) null).show();
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

        fab = binding.backupSettings;
        fab.setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                boolean res = false;
                ObjectOutputStream output = null;
                File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/Vanilla_backup");
                try {
                    boolean p = false;
                    if(!f.exists()) {
                        p = f.createNewFile();
                    } else p = true;

                    if(p){
                        output = new ObjectOutputStream(new FileOutputStream(f));
                        output.writeObject(prefs.getAll());

                        res = true;
                    }
                    Toast.makeText(this, "Okay", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (IOException ex) {
                        Toast.makeText(this, "Error №2: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                    }
                }
            } else {
                new MaterialAlertDialogBuilder(this).setTitle("Storage permissions").setMessage("For:\n● Wallpaper preview\n● Backup settings").setIcon(R.drawable.ic_sd_card).setPositiveButton("Let's enable", (dialogInterface, i) -> {
                    ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 1);
                }).setNeutralButton("Report bug", (dialogInterface, i) -> {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ServOKio/vanilla")));
                }).setNegativeButton("Ignore", null).show();
            }
        });

        fab = binding.restoreSettings;
        fab.setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/Vanilla_backup");
                ObjectInputStream input = null;
                try {
                    input = new ObjectInputStream(new FileInputStream(f));
                    SharedPreferences.Editor prefEdit = prefs.edit();
                    prefEdit.clear();
                    Map<String, ?> entries = (Map<String, ?>) input.readObject();
                    for (Map.Entry<String, ?> entry : entries.entrySet()) {
                        Object v = entry.getValue();
                        String key = entry.getKey();

                        if (v instanceof Boolean)
                            prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                        else if (v instanceof Float)
                            prefEdit.putFloat(key, ((Float) v).floatValue());
                        else if (v instanceof Integer)
                            prefEdit.putInt(key, ((Integer) v).intValue());
                        else if (v instanceof Long)
                            prefEdit.putLong(key, ((Long) v).longValue());
                        else if (v instanceof String)
                            prefEdit.putString(key, ((String) v));
                    }
                    prefEdit.commit();
                    Toast.makeText(this, "Okay", Toast.LENGTH_SHORT).show();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Toast.makeText(this, "Error №2: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                new MaterialAlertDialogBuilder(this).setTitle("Storage permissions").setMessage("For:\n● Wallpaper preview\n● Backup settings").setIcon(R.drawable.ic_sd_card).setPositiveButton("Let's enable", (dialogInterface, i) -> {
                    ActivityCompat.requestPermissions(MainActivity.getInstance(), new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 1);
                }).setNeutralButton("Report bug", (dialogInterface, i) -> {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/ServOKio/vanilla")));
                }).setNegativeButton("Ignore", null).show();
            }
        });

        //debug
        //startActivity(new Intent(this, Test.class));
    }

    @SuppressWarnings({ "unchecked" })
    private boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = prefs.edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    prefEdit.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    prefEdit.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    prefEdit.putLong(key, (Long) v);
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.apply();
            res = true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
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

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

}