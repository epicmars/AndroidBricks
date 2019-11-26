package com.androidpi.app.bricks.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.TypedValue;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import timber.log.Timber;

/** Created by on 2019/1/10 */
public class ViewHelper {

    public static void removeLeftMargin(View child) {
        if (child == null) return;
        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        marginLayoutParams.leftMargin = 0;
        child.setLayoutParams(marginLayoutParams);
    }

    public static void removeRightMargin(View child) {
        if (child == null) return;
        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        marginLayoutParams.rightMargin = 0;
        child.setLayoutParams(marginLayoutParams);
    }

    public static void removeHorizontalMargins(View child) {
        if (child == null) return;
        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        marginLayoutParams.leftMargin = 0;
        marginLayoutParams.rightMargin = 0;
        child.setLayoutParams(marginLayoutParams);
    }

    public static void removeHorizontalChildMargins(ViewGroup parent) {
        if (parent == null) return;
        int count = parent.getChildCount();
        if (count == 1) {
            removeHorizontalMargins(parent.getChildAt(0));
        } else if (count > 1) {
            int firstChildIndex = 0;
            int lastChildIndex = count - 1;
            for (int i = firstChildIndex; i < count; i++) {
                View child = parent.getChildAt(i);
                if (View.VISIBLE == child.getVisibility()) {
                    removeLeftMargin(child);
                    break;
                } else {
                    removeHorizontalMargins(child);
                }
            }
            for (int i = lastChildIndex; i >= 0; i--) {
                View child = parent.getChildAt(i);
                if (View.VISIBLE == child.getVisibility()) {
                    removeRightMargin(child);
                    break;
                } else {
                    removeHorizontalMargins(child);
                }
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result =
                Math.round(
                        TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                24,
                                context.getResources().getDisplayMetrics()));
        int resourceId =
                context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public interface BitmapCreateCallback {
        void onBitmapCreated(Bitmap bitmap);
    }

    public static void createBitmapFromView(
            View view, Window window, BitmapCreateCallback callback) {
        createBitmapFromView(view, window, null, callback);
    }

    public static void createBitmapFromView(
            View view, Window window, Rect dest, BitmapCreateCallback callback) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] locationInWindow = new int[2];
            view.getLocationInWindow(locationInWindow);
            Rect rect =
                    new Rect(
                            locationInWindow[0],
                            locationInWindow[1],
                            locationInWindow[0] + width,
                            locationInWindow[1] + height);
            if (dest != null) {
                rect = dest;
            }
            try {
                PixelCopy.request(
                        window,
                        rect,
                        bitmap,
                        new PixelCopy.OnPixelCopyFinishedListener() {
                            @Override
                            public void onPixelCopyFinished(int copyResult) {
                                switch (copyResult) {
                                    case PixelCopy.SUCCESS:
                                        callback.onBitmapCreated(bitmap);
                                        break;
                                    default:
                                        Timber.e("PixCopy failed: %d", copyResult);
                                        break;
                                }
                            }
                        },
                        new Handler());
            } catch (IllegalArgumentException e) {

            }
        } else {
            view.buildDrawingCache();
            Bitmap bitmap = view.getDrawingCache();
            if (dest == null) {
                callback.onBitmapCreated(bitmap);
                return;
            }
            Bitmap result =
                    Bitmap.createBitmap(bitmap, dest.left, dest.top, dest.width(), dest.height());
            bitmap.recycle();
            callback.onBitmapCreated(result);
        }
    }
}
