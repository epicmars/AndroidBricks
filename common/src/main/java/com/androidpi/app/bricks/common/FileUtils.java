package com.androidpi.app.bricks.common;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class FileUtils {

    /**
     * get file directory from specific file path
     *
     * @param filePath
     * @return
     */
    public static String getDirectory(String filePath) {

        int lastSeparator = filePath.lastIndexOf(File.separatorChar);
        if (lastSeparator != -1) {
            return filePath.substring(0, lastSeparator + 1);
        } else {
            throw new IllegalArgumentException("<" + filePath + "> is not a valid file path!");
        }
    }

    /**
     * get file name from specific file path
     *
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        int lastSeparator = filePath.lastIndexOf(File.separatorChar);
        if (lastSeparator != -1) {
            return filePath.substring(lastSeparator, filePath.length());
        } else {
            throw new IllegalArgumentException("<" + filePath + "> is not a valid file path!");
        }
    }

    public static long getLastModified(String filePath) {
        File file = new File(filePath);
        return file.lastModified();
    }

    public static boolean copyFile(String from, String to) {
        File src = new File(from);
        if (src.exists() && src.isFile()) {
            BufferedInputStream in = null;
            BufferedOutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(src));
                out = new BufferedOutputStream(new FileOutputStream(new File(to)));
                byte[] buffer = new byte[4096];
                int len = -1;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static File getAppFileDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || Environment.isExternalStorageRemovable()) {
            return context.getExternalFilesDir(null);
        } else {
            return context.getFilesDir();
        }
    }

    public static File getAppImageDir(Context context) {
        File dir = getAppFileDir(context);
        if (dir != null) {
            File imageDir = new File(dir, "images");
            if (!imageDir.exists()) {
                if (imageDir.mkdirs()) {
                    return imageDir;
                }
            }
            return imageDir;
        }
        return null;
    }

    /**
     * 创建目录
     * @param path
     * @return
     */
    public static boolean mkdir(String path){
        File file = new File(path);
        if(!file.exists()){
            return file.mkdirs();
        }
        return false;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(int type) {
        File photoDir = getOutputMediaFile(type);
        // TODO 检查文件夹是否为空
        return Uri.fromFile(photoDir);
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DCIM), "Meishikr");
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        // This location works best if you want the created images to be shared
        // between applications and save after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Meishikr", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "MSK_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }


    public static void rm(File file) throws IOException {
        if (file == null)
            return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                rm(f);
            }
            file.delete();
        }
    }
}