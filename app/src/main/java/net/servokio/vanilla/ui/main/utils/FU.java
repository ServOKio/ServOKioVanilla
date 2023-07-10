package net.servokio.vanilla.ui.main.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FU {
    public static File saveImage(Context context, String to, Uri uri) {
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            File file = new File(to);
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
            Log.e("FileProvider", "Save image failed  " + uri);
        }
        return null;
    }

    public static File saveImage(Context context, String to, Bitmap bitmap) {
        try {
//            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            File f = new File(to);
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            f.setReadable(true, false);
            fos.close();
            return f;
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            byte[] bArr = new byte[8192];
//            while (true) {
//                int read = openInputStream.read(bArr);
//                if (read != -1) {
//                    fileOutputStream.write(bArr, 0, read);
//                } else {
//                    fileOutputStream.flush();
//                    file.setReadable(true, false);
//                    return file;
//                }
//            }
        } catch (IOException unused) {
            Log.e("FileProvider", "Save image failed  ");
        }
        return null;
    }

    public static Bitmap getContactPhoto(Context context, String contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return BitmapFactory.decodeStream(fd.createInputStream());
        } catch (IOException e) {
            return null;
        }
    }
}
