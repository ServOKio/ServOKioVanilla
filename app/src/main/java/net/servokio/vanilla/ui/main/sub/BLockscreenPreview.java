package net.servokio.vanilla.ui.main.sub;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;

import java.io.File;

public class BLockscreenPreview extends AppCompatActivity {
    private boolean debug = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.sub_lockscreen_preview);

        if(!debug){
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.getInstance());
            if (Build.VERSION.SDK_INT >= 24 && ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                ParcelFileDescriptor pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
                if (pfd == null) pfd = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                if (pfd != null) {
                    ImageView imageView = findViewById(R.id.wallpaper1);
                    final Bitmap result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    imageView.setImageBitmap(result);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.test);
                    imageView.startAnimation(animation);
                }
            }
        } else {
            File file = new File("/storage/emulated/0/Pictures/1851fc04f515e77fec424636ead338724f3efb89.gif");
            if (file.exists()) {
                ImageView imageView = findViewById(R.id.wallpaper1);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                Glide.with(imageView.getContext()).load(file).into(imageView);
            }
            File file2 = new File("/storage/emulated/0/Pictures/Untitled-1123.png");
            if (file2.exists()) {
                ImageView imageView = findViewById(R.id.wallpaper2);
                int boxHeight = ((View)imageView.getParent()).getMeasuredHeight();
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                Glide.with(imageView.getContext()).load(file2).into(imageView);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(file2.getAbsolutePath(), options);
//                int imageHeight = options.outHeight;
//                int imageWidth = options.outWidth;
//                imageView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//                    int height = imageView.getMeasuredHeight();
//                    imageView.setY(-(imageHeight - boxHeight));
//                });
            }
        }
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.about_phone, new AAboutDevice.SettingsFragment()).commit();
//        }
    }
}
