package net.servokio.vanilla.modules;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLConnection;

public class Static {
    @SuppressLint("PrivateApi")
    public static String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public static String currentVersion(){
        double release=Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)","$1"));
        String codeName="Unsupported";//below Jelly Bean
        if(release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if(release < 5)   codeName="Kit Kat";
        else if(release < 6)   codeName="Lollipop";
        else if(release < 7)   codeName="Marshmallow";
        else if(release < 8)   codeName="Nougat";
        else if(release < 9)   codeName="Oreo";
        else if(release < 10)  codeName="Pie";
        else if(release >= 10) codeName="Android "+((int)release);//since API 29 no more candy code names
        return codeName+" v"+release+", API Level: "+Build.VERSION.SDK_INT;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String getFileMine(ContentResolver cr, Uri uri){
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getPreferenceDir(Context context) {
        String mPreferenceDir = null;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("dummy", 0);
            sharedPreferences.edit().putBoolean("dummy", false).commit();
            Field declaredField = sharedPreferences.getClass().getDeclaredField("mFile");
            declaredField.setAccessible(true);
            mPreferenceDir = new File(((File) declaredField.get(sharedPreferences)).getParent()).getAbsolutePath();
            Log.d("Vanilla", "Preference folder: " + mPreferenceDir);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e("Vanilla", "Could not determine preference folder path. Returning default.");
            e.printStackTrace();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mPreferenceDir = context.getDataDir() + "/shared_prefs";
            }
        }
        return mPreferenceDir;
    }
}
