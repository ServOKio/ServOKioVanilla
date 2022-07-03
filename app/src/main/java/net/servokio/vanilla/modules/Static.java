package net.servokio.vanilla.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static String getFileMine(File file){
        try {
            URLConnection connection = file.toURL().openConnection();
            String mimeType = connection.getContentType();
            return mimeType;
        } catch (IOException e) {
           return null;
        }
    }
}
