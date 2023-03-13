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
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.preferences.CustomPreviewImagePreference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.nio.file.Files;

public class AHeader extends AppCompatActivity{
    private static final int REQUEST_PICK_IMAGE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_header);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.header, new SettingsFragment()).commit();
        }
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
                File file = saveHeaderImage(imageUri);
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

        private File saveHeaderImage(Uri uri) {
            try {
                InputStream openInputStream = getContext().getContentResolver().openInputStream(uri);
                File file = new File(Static.getPreferenceDir(getActivity()) + "/custom_file_header_image");
                if (file.exists()) file.delete();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = openInputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        fileOutputStream.flush();
                        file.setReadable(true, false);
                        return file;
                    }
                }
            } catch (IOException unused) {
                Log.e("FileHeaderProvider", "Save header image failed  " + uri);
            }
            return null;
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
