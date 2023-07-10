package net.servokio.vanilla.ui.main.sub;

import static net.servokio.vanilla.MainActivity.prefs;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.preferences.CustomPreviewImagePreference;
import net.servokio.vanilla.ui.main.Intents;
import net.servokio.vanilla.ui.main.utils.FU;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Arrays;

public class AHeader extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final int REQUEST_PICK_IMAGE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.holder_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.holder_main, new SettingsFragment()).commit();
        }
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Vanilla", "key "+key);
        if(key.equals("status_bar_custom_header_height")){
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE_PREFS);
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE_HEIGHT);
        } else if (key.equals("file_header_select")) {
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE_PREFS);
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE);
        } else if (Arrays.asList("status_bar_custom_header").contains(key)) {
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE_PREFS);
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE_HEIGHT);
            sendIntent(Intents.ACTION_UPDATE_HEADER_IMAGE);
        }
    }

    private void sendIntent(String aga){
        Log.d("Vanilla", "send intent "+aga);
        final Intent intent = new Intent();
        intent.setAction(aga);
        sendBroadcast(intent);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_header, rootKey);

            CustomPreviewImagePreference pref = findPreference("file_header_select");
            if (pref != null) {
                System.out.println("PD: "+Static.getPreferenceDir(getActivity()) + "/custom_file_header_image");
                pref.setFilePath(Static.getPreferenceDir(getActivity()) + "/custom_file_header_image");
                pref.setOnPreferenceClickListener(e -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/* video/mp4");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                    return true;
                });
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent result) {
            if (resultCode != Activity.RESULT_OK) return;
            if (requestCode == REQUEST_PICK_IMAGE) {
                final Uri imageUri = result.getData();

                SharedPreferences.Editor editor = prefs.edit();
                File file = FU.saveImage(getContext(), Static.getPreferenceDir(getActivity()) + "/custom_file_header_image", imageUri);
                if(file != null){
                    String mine = Static.getFileMine(getContext().getContentResolver(), imageUri);
                    editor.putString("status_bar_custom_header_image_type", mine == null ? "unk" : mine);
                } else {
                    System.out.println("fail");
                    editor.putString("status_bar_custom_header_image_type", "unk");
                }

                editor.putString("status_bar_custom_header_provider", "file");
                editor.putString("ctx_files_dir", Static.getPreferenceDir(getActivity()) + "/custom_file_header_image");
                editor.putString("status_bar_custom_header_image", imageUri.toString());
                editor.apply();
                System.out.println("New banner image");
            } else System.out.println(requestCode);
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
                }

            });
            return recyclerView;
        }
    }
}
