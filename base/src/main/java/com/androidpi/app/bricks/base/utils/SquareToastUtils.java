package com.androidpi.app.bricks.base.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.androidpi.app.bricks.base.widgets.SquareToastView;

/**
 * Created by on 2018/12/29
 */
public class SquareToastUtils {

    public static void shortToast(Context context, @DrawableRes int icRes, @StringRes int strRes) {
        showToast(context, icRes, strRes, Toast.LENGTH_SHORT);
    }

    public static void longToast(Context context, @DrawableRes int icRes, @StringRes int strRes) {
        showToast(context, icRes, strRes, Toast.LENGTH_LONG);
    }

    public static void showToast(Context context, @DrawableRes int icRes, @StringRes int strRes, int duration) {
        SquareToastView view = new SquareToastView(context);
        view.setIcon(icRes);
        view.setMsg(strRes);
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void shortToast(Context context, @DrawableRes int icRes, String msg) {
        showToast(context, icRes, msg, Toast.LENGTH_SHORT);
    }

    public static void longToast(Context context, @DrawableRes int icRes, String msg) {
        showToast(context, icRes, msg, Toast.LENGTH_LONG);
    }

    public static void showToast(Context context, @DrawableRes int icRes, String msg, int duration) {
        SquareToastView view = new SquareToastView(context);
        view.setIcon(icRes);
        view.setMsg(msg);
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
