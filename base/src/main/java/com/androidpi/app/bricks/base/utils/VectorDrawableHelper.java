package com.androidpi.app.bricks.base.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

@Deprecated
public class VectorDrawableHelper {

    public static VectorDrawableCompat getVectorDrawable(Context context, @DrawableRes int vectorRes) {
        return VectorDrawableCompat.create(context.getResources(), vectorRes, context.getTheme());
    }

    public static void setTextDrawable(TextView textView, Drawable left, Drawable top, Drawable right, Drawable bottom) {
        textView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }

    public static void setTextVectorDrawableLeft(TextView textView, @DrawableRes int vectorRes) {
        setTextDrawable(textView, getVectorDrawable(textView.getContext(), vectorRes), null, null, null);
    }

    public static void setTextVectorDrawableTop(TextView textView, @DrawableRes int vectorRes) {
        setTextDrawable(textView, null, getVectorDrawable(textView.getContext(), vectorRes), null, null);
    }

    public static void setTextVectorDrawableRight(TextView textView, @DrawableRes int vectorRes) {
        setTextDrawable(textView, null, null, getVectorDrawable(textView.getContext(), vectorRes), null);
    }

    public static void setTextVectorDrawableBottom(TextView textView, @DrawableRes int vectorRes) {
        setTextDrawable(textView, null, null, null, getVectorDrawable(textView.getContext(), vectorRes));
    }
}
