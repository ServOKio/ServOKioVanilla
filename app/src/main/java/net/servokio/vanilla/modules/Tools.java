package net.servokio.vanilla.modules;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Tools {
    public static void bootloader() {
        rebootPhone("bootloader");
    }

    public static void bootloader(Context context) {
        rebootPhone(context, "bootloader");
    }

    public static void dispatch(Context context, String[] strArr) {
        if (strArr[0].contentEquals("reboot")) {
            reboot(context);
        } else if (strArr[0].contentEquals("recovery")) {
            recovery(context);
        } else if (strArr[0].contentEquals("download")) {
            download(context);
        } else if (strArr[0].contentEquals("bootloader")) {
            bootloader(context);
        } else if (strArr[0].contentEquals("hotboot")) {
            hotboot();
        } else if (strArr[0].contentEquals("restartsystemui")) {
            rebootSystemUi();
        } else if (!strArr[0].contentEquals("shell")) {
        } else {
            if (strArr.length < 2) {
                Log.e("Tools", "Not enough parameters given for SHELL");
            } else {
                shell(strArr[1]);
            }
        }
    }

    public static void dispatch(String[] strArr) {
        if (strArr[0].contentEquals("reboot")) {
            reboot();
        } else if (strArr[0].contentEquals("recovery")) {
            recovery();
        } else if (strArr[0].contentEquals("download")) {
            download();
        } else if (strArr[0].contentEquals("bootloader")) {
            bootloader();
        } else if (strArr[0].contentEquals("hotboot")) {
            hotboot();
        } else if (strArr[0].contentEquals("restartsystemui")) {
            rebootSystemUi();
        } else if (!strArr[0].contentEquals("shell")) {
        } else {
            if (strArr.length < 2) {
                Log.e("Tools", "Not enough parameters given for SHELL");
            } else {
                shell(strArr[1]);
            }
        }
    }

    public static void download() {
        rebootPhone("download");
    }

    public static void download(Context context) {
        rebootPhone(context, "download");
    }

    private static String getSuBin() {
        return new File("/system/fbin", "fu").exists() ? "/system/fbin/fu" : "su";
    }

    public static void hotboot() {
        shell("setprop ctl.restart surfaceflinger;setprop ctl.restart zygote");
    }

    private static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void reboot() {
        rebootPhone("now");
    }

    public static void reboot(Context context) {
        rebootPhone(context, "now");
    }

    public static void rebootPhone(Context context, String str) {
        try {
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(str);
        } catch (Exception e) {
            Log.e("Tools", "reboot '" + str + "' error: " + e.getMessage());
            shell("reboot " + str);
        }
    }

    private static void rebootPhone(String str) {
        shell("reboot " + str);
    }

    public static void rebootSystemUi() {
        shell("pkill -TERM -f com.android.systemui");
    }

    public static void recovery() {
        rebootPhone("recovery");
    }

    public static void recovery(Context context) {
        rebootPhone(context, "recovery");
    }

    public static String shell(String str) {
        Iterator<String> it = null;
        String str2 = "";
        while (system(getSuBin(), str).getStringArrayList("out").iterator().hasNext()) {
            str2 = str2 + it.next() + "\n";
        }
        return str2;
    }

    private static Bundle system(String str, String str2) {
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<String> arrayList2 = new ArrayList<>();
        boolean z = false;
        try {
            Process exec = Runtime.getRuntime().exec(str);
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
            dataOutputStream.writeBytes(String.valueOf(str2) + "\n");
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            exec.waitFor();
            if (exec.exitValue() == 255) {
                arrayList2.add("SU was probably denied! Exit valie is 255");
            }
            while (bufferedReader.ready()) {
                arrayList.add(bufferedReader.readLine());
            }
            while (bufferedReader2.ready()) {
                arrayList2.add(bufferedReader2.readLine());
            }
            exec.destroy();
            z = arrayList2.size() <= 0;
        } catch (IOException e) {
            arrayList2.add("IOException: " + e.getMessage());
        } catch (InterruptedException e2) {
            arrayList2.add("InterruptedException: " + e2.getMessage());
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", z);
        bundle.putString("cmd", str2);
        bundle.putString("binary", str);
        bundle.putStringArrayList("out", arrayList);
        bundle.putStringArrayList("error", arrayList2);
        return bundle;
    }

}
