package net.servokio.vanilla.ui.main.sub;

import static net.servokio.vanilla.MainActivity.prefs;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import net.servokio.vanilla.MainActivity;
import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.FontListParser;
import net.servokio.vanilla.modules.Static;
import net.servokio.vanilla.modules.Tools;
import net.servokio.vanilla.preferences.CustomPreviewImagePreference;
import net.servokio.vanilla.preferences.CustomSeekBarPreference;
import net.servokio.vanilla.ui.main.utils.FU;
import net.servokio.vanilla.ui.main.utils.MUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class ALockScreenUI extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.holder_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.holder_main, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
        private LinearLayout l = null;
        private boolean v = true;
        private ImageView imageView = null;
        //Some shit
        private ImageView mLockIcon = null;
        private TextClock clockView = null;
        private TextView mOwnerInfo = null;
        private float alpha = 0;

        @Override
        public void onResume(){
            super.onResume();
            MainActivity.getInstance().prefs.registerOnSharedPreferenceChangeListener(this);
            updateClock();
        }
        @Override
        public void onPause(){
            MainActivity.getInstance().prefs.unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_sub_lock_screen_ui, rootKey);

            Preference pref = findPreference("lockscreen_widgets_list");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), WidgetsList.class));
                return true;
            });

            pref = findPreference("lockscreen_clock_position");
            if (pref != null) {
                DisplayMetrics metrics = this.getResources().getDisplayMetrics();
                ((CustomSeekBarPreference) pref).setMax(metrics.heightPixels);
            }

            pref = findPreference("lockscreen_colors");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                startActivity(new Intent(getContext(), ALockScreenColors.class));
                return true;
            });

            pref = findPreference("lockscreen_click");
            if (pref != null) pref.setOnPreferenceClickListener(e -> {
                play();
                return true;
            });

            pref = findPreference("lockscreen_video_background_file");
            if (pref != null) {
                pref.setOnPreferenceClickListener(e -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/* video/mp4");
                    startActivityForResult(intent, 1);
                    return true;
                });
            }

            ListPreference pref1 = findPreference("lock_ownerinfo_fonts");
            if(pref != null){
                List<FontListParser.SystemFont> fonts;
                try {
                    fonts = FontListParser.getSystemFonts();
                } catch (Exception e) {
                    pref1.setDialogMessage("Sys error: "+e.getMessage());
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
                pref1.setEntries(entries);
                pref1.setEntryValues(entryValues);
            }

            //Owner info
            pref = findPreference("lockscreen_profile_icon");
//            UserManager userManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
//            pref.setSummary(userManager.getUserName());


//            final AccountManager manager = AccountManager.get(getContext());
//            final Account[] accounts = manager.getAccounts();
//            StringBuilder f = new StringBuilder();
//            for(Account account : accounts){
//                f.append(account.name).append(", ");
//            }
//            pref.setSummary(f);

            final String[] SELF_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, };
            Cursor cursor = getActivity().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, SELF_PROJECTION, null, null, null);
            cursor.moveToFirst();
            pref.setSummary(cursor.getString(1));

            Bitmap draw = FU.getContactPhoto(getContext(), cursor.getString(0));
            if(draw != null){
                Drawable d = new BitmapDrawable(getResources(), MUtils.getRoundedCroppedBitmap(draw));
                pref.setIcon(d);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent result) {
            if (resultCode != Activity.RESULT_OK) return;
            if (requestCode == 1) {
                final Uri videoUri = result.getData();

                SharedPreferences.Editor editor = prefs.edit();
                File file = FU.saveImage(getContext(), Static.getPreferenceDir(getActivity()) + "/custom_lockscreen_video", videoUri);
                if(file != null){
                    editor.putString("lockscreen_custom_video_type", "mp4");
                } else {
                    System.out.println("fail");
                    editor.putString("lockscreen_custom_video_type", "unk");
                }

                //editor.putString("status_bar_custom_header_provider", "file");
                //editor.putString("ctx_files_dir", Static.getPreferenceDir(getActivity()) + "/custom_lockscreen_video");
                //editor.putString("status_bar_custom_header_image", videoUri.toString());
                editor.apply();
                System.out.println("New lockscreen video");
            } else System.out.println(requestCode);
        }

        void play(){
            if(l == null) return;
            if(this.v){
                AlphaAnimation anim = new AlphaAnimation(alpha, 1);
                anim.setDuration(350);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {l.setAlpha(1);}
                    @Override
                    public void onAnimationEnd(Animation animation) {}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                if(MainActivity.prefs.getBoolean("lockscreen_wallpaper_zoom", true)) zoomNorm();
                l.startAnimation(anim);
            } else {
                screenON();
            }
            this.v = !this.v;
        }

        void screenON(){
            l.animate().alpha(alpha).setDuration(350);

            if(MainActivity.prefs.getBoolean("lockscreen_wallpaper_zoom", true)) zoomBig();
        }

        void zoomNorm(){
            ScaleAnimation animation = new ScaleAnimation(1.1f, 1, 1.1f, 1, ScaleAnimation.RELATIVE_TO_SELF,(float)0.5,ScaleAnimation.RELATIVE_TO_SELF,(float)0.5);
            animation.setDuration(350);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {imageView.setScaleX(1);imageView.setScaleY(1);}
                @Override
                public void onAnimationEnd(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            imageView.startAnimation(animation);
        }

        void zoomBig(){
            ScaleAnimation animation = new ScaleAnimation(1, 1.1f, 1, 1.1f, ScaleAnimation.RELATIVE_TO_SELF,(float)0.5,ScaleAnimation.RELATIVE_TO_SELF,(float)0.5);
            animation.setDuration(350);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {imageView.setScaleX(1.1f);imageView.setScaleY(1.1f);}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            imageView.startAnimation(animation);
        }

        void updateClock(){
            if(clockView == null) return;
            clockView.setRotation(MainActivity.getInstance().prefs.getInt("lockscreen_clock_rotation", 0));
            if(MainActivity.getInstance().prefs.getBoolean("lockscreen_clock_color_enable", false)) clockView.setTextColor(MainActivity.getInstance().prefs.getInt("lockscreen_clock_color", 0xffffffff));
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
                        imageView = getListView().findViewById(R.id.imageView4);
                        mLockIcon = getListView().findViewById(R.id.lock_icon);
                        clockView = getListView().findViewById(R.id.clock_view);
                        mOwnerInfo = getListView().findViewById(R.id.owner_info);
                        if(!MainActivity.prefs.getString("owner_info_settings", "").equals("")){
                            mOwnerInfo.setText(MainActivity.prefs.getString("owner_info_settings", ""));
                        }
                        mLockIcon.setVisibility(MainActivity.prefs.getBoolean("lockscreen_lock_icon", true) ? View.VISIBLE : View.GONE);
                        if(!MainActivity.prefs.getBoolean("lockscreen_clock", true)) clockView.setAlpha(0);
                        updateClock();
                        final Bitmap result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        imageView.setImageBitmap(result);
                        if(MainActivity.prefs.getBoolean("lockscreen_wallpaper_zoom", true)){
                            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.test);
                            imageView.startAnimation(animation);
                        }
                        l = getListView().findViewById(R.id.black);
                        alpha = (100 - MainActivity.prefs.getInt("lockscreen_bg_opacity", 55)) / 100.0f;
                        screenON();
                    }
                }

            });
            return recyclerView;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            System.out.println(key);
            if (key.equals("lockscreen_bg_opacity") && l != null) {
                alpha = (100 - sharedPreferences.getInt("lockscreen_bg_opacity", 55)) / 100.0f;
                l.setAlpha(alpha);
            } else if (key.equals("lockscreen_wallpaper_zoom") && imageView != null) {
                if(sharedPreferences.getBoolean("lockscreen_wallpaper_zoom", true)){
                    zoomBig();
                } else zoomNorm();
            } else if (key.equals("lockscreen_clock_rotation") && clockView != null) {
                updateClock();
            } else if (key.equals("lockscreen_clock") && clockView != null) {
                boolean yes = sharedPreferences.getBoolean("lockscreen_clock", true);
                clockView.animate().alpha(yes ? 1 : 0).setDuration(250);
            } else if (key.equals("owner_info_settings") && mOwnerInfo != null) {
                mOwnerInfo.setText(sharedPreferences.getString("owner_info_settings", ""));
            }
        }
    }


//    private Drawable encircle(Bitmap bitmap) {
//        return CircleFramedDrawable.getInstance(getActivity(), bitmap);
//    }

}