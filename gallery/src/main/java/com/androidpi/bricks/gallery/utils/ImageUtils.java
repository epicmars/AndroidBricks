package com.androidpi.bricks.gallery.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class ImageUtils {

    public static final int REQ_LENGTH = 1280;

    public static void saveBitmap(String path, Bitmap bitmap){
        FileOutputStream out = null;
        try{
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (FileNotFoundException e){
            Timber.e(e,"File not found!");
        } finally {
            if(null != out){
                try{
                    out.close();
                } catch (IOException e){
                    Timber.e(e, "Close output stream error!");
                }
            }
        }
    }

    public static Bitmap resizeImage(String imageFile){
        return resizeImage(imageFile, REQ_LENGTH, REQ_LENGTH);
    }

    public static Bitmap resizeImage(String imageFile, int reqWidth, int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageFile, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int largestLength){
        return calculateInSampleSize(options, largestLength, largestLength);
    }

    /**
     * Calculate an inSampleSize for use in a
     * {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from
     * {@link android.graphics.BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the
     * final decoded bitmap having a width and height equal to or larger than
     * the requested width and height.
     *
     * @param options   An options object with out* params already populated (run
     *                  through a decode* method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final float height = options.outHeight;
        final float width = options.outWidth;
        int inSampleSize = 1;

        if(height > width){
            inSampleSize = Math.round(height / reqHeight);
        } else {
            inSampleSize = Math.round(width / reqWidth);
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }
}