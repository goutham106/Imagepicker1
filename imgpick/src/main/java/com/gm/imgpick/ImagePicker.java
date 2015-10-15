package com.gm.imgpick;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gowtham on 14/10/15.
 */
public class ImagePicker {

    public String TAG = "ImagePicker";
    // private String filePath = "/ImagePicker";// "/Files";
    private static ImagePicker ImagePickerObject = null;
    private static Context context;

    private File mediaStorageDir = null;


    private Bitmap.CompressFormat compressFormat = null;
    private int imageQuality = -1;


    public static ImagePicker getInstance(Context con) {
        if (ImagePickerObject == null) {
            ImagePickerObject = new ImagePicker();
            context = con;
            return ImagePickerObject;
        }
        return ImagePickerObject;
    }

    public static ImagePicker getInstance() {
        if (ImagePickerObject == null) {
            ImagePickerObject = new ImagePicker();
            return ImagePickerObject;
        }
        return ImagePickerObject;
    }

    /**
     * Initilazing Methods
     */


    public void initTempFilePath(String filePath) {

        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            this.mediaStorageDir = new File(
                    Environment.getExternalStorageDirectory(),
                    filePath);
            //context.getResources().getString(R.string.app_name)+"CatchTemp");//"ImagePickCacheTemp");
        } else {
            this.mediaStorageDir = context.getCacheDir();
        }
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

    }

    public void initBitmapConfig(Bitmap.CompressFormat compressFormat, int imgQuality) {

        this.compressFormat = compressFormat;
        this.imageQuality = imgQuality;
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @author paulburke
     */
    public String getPath(final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                switch (type) {
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(contentUri, selection,
                        selectionArgs);
            } else if (isdriveDoc(uri)) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

                return getDataColumn(uri, null, null);

            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 500) ? (originalSize / 500) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }


    /**
     *
     *
     * @param image
     * @return uri
     */
    public Uri storeImage(Bitmap image) {
        if (mediaStorageDir == null) {
            Toast.makeText(context, context.getString(R.string.initTempFilePath), Toast.LENGTH_SHORT).show();
            return null;
        }

        File pictureFile = getOutputMediaFile(context);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(pictureFile);
    }


    public Uri storeFile(Uri uri, String extension) {

        if (mediaStorageDir == null) {
            Toast.makeText(context, context.getString(R.string.initTempFilePath), Toast.LENGTH_SHORT).show();
            return null;
        }

        File resultFile = getOutputMediaFile(context, extension);
        if (resultFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
//            File file = new File(uri.toString());
            InputStream in = context.getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(resultFile.getPath());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(resultFile);
    }


    public void clearCatch() {
//        File catchStorageDir = new File(Environment.getExternalStorageDirectory()
//                + "/Android/data/"
//                + context.getApplicationContext().getPackageName()
//                + filePath);

        if (mediaStorageDir.exists()) {
            String[] children = mediaStorageDir.list();
            for (String s : children)
                Log.i(TAG, "***/ANDROID/data/APP_PACKAGE/" + s + "***");
        }
        Deletecatch(mediaStorageDir);

    }


    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(
                        targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }


    public Bitmap compressBitmap(String filePath, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmpFactoryOptions);
        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
                / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
                / (float) width);
        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio)
                bmpFactoryOptions.inSampleSize = heightRatio;
            else
                bmpFactoryOptions.inSampleSize = widthRatio;
        }
        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filePath, bmpFactoryOptions);
        return bitmap;
    }

    public void createDirectory(String Directory) {
        File temp = new File(Directory);
        if (!temp.exists() && !temp.isDirectory()) {
            if (!temp.mkdirs())
                Log.w(TAG, "Problem in creating a directory");
        }
    }

    public void saveImage(Context context, String url, File path) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(path, filename);
        if (!f.exists()) {
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl
                        .openConnection();
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(f);
                CopyStream(is, os);
                os.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public File createFile(String Path) {
        return new File(Path);
    }


    public File addImagefileInStream(String outputImagePath, Bitmap bitmap,
                                     int quality, Bitmap.CompressFormat compressFormat) {
        File f = new File(outputImagePath);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, quality, bos);

        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public File addImagefileInStream(String outputImagePath, Bitmap bitmap) {
        if (compressFormat == null || imageQuality < 0) {
            Toast.makeText(context, context.getString(R.string.initBitmapConfig), Toast.LENGTH_SHORT).show();
        } else {
            File f = new File(outputImagePath);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(compressFormat, imageQuality, bos);

            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                return f;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static File addImagefileInStream(String outputImagePath, byte[] bitmapdata) {
        File f = new File(outputImagePath);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Supporting Methods
     */


    private void Deletecatch(File dir) {
        Log.d(TAG, "DELETE_PREVIOUS TOP" + dir.getPath());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File temp = new File(dir, children[i]);
                if (temp.isDirectory()) {
                    Log.d(TAG, "Recursive Call" + temp.getPath());
                    Deletecatch(temp);
                } else {
                    Log.d(TAG, "Delete File" + temp.getPath());
                    boolean b = temp.delete();
                    if (b == false) {
                        Log.d(TAG, "DELETE FAIL");
                    }
                }
            }
        }
        dir.delete();
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    private boolean isdriveDoc(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri
                .getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private String getDataColumn(Uri uri,
                                 String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }


    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(Context context) {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

        return mediaFile;
    }


    private File getOutputMediaFile(Context context, String extension) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + extension;//".pdf";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

}
