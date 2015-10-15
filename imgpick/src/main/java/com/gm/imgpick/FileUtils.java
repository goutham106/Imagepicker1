package com.gm.imgpick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gowtham on 15/10/15.
 */
public class FileUtils {

    static public File getPath(Context context) {

        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(
                    Environment.getExternalStorageDirectory(),
                    getApplicationName(context)+"CatchTemp");
                    //context.getResources().getString(R.string.app_name)+"CatchTemp");//"ImagePickCacheTemp");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }




//////////////////




    public static void delete_files(File Path) {
        //TODO : Gm 8-June-2015
        //File temp = new File(Path);
        File temp = Path;
        if (temp.isDirectory()) {
            String[] children = temp.list();
            for (int i = 0; i < children.length; i++) {
                new File(temp, children[i]).delete();
            }
        } else {
            temp.deleteOnExit();
        }
    }

//    public static ArrayList<String> filter_available_files(
//            ArrayList<String> imageUrl) {
//        ArrayList<String> temp = new ArrayList<String>();
//        for (int i = 0; i < imageUrl.size(); i++) {
//            File image = new File(imageUrl.get(i));
//            if (image.exists())
//                temp.add(imageUrl.get(i));
//        }
//        return temp;
//    }



//    public static void loadImageDetail(File imageFile, Context context) {
//
//        ContentValues image = new ContentValues();
//
//        image.put(Images.Media.MIME_TYPE, "image/png");
//        image.put(Images.Media.ORIENTATION, 0);
//
//        File parent = imageFile.getParentFile();
//        String path = parent.toString().toLowerCase();
//        String name = parent.getName().toLowerCase();
//        image.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
//        image.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
//        image.put(Images.Media.SIZE, imageFile.length());
//
//        image.put(Images.Media.DATA, imageFile.getAbsolutePath());
//
//        Uri result = context.getContentResolver().insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
//    }

}
