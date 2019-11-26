package com.androidpi.app.bricks.base.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputMethodUtils {

    public static void hideInputMethod(Context context, View view) {
        if (context == null)
            return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean showSoftInputMethod(Context context, View view) {
        if (context == null)
            return false;
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.showSoftInput(view, 0);
        }
        return false;
    }
}