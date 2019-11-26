package com.androidpi.app.bricks.utils;

import android.view.View;

public class AntiDoubleClickHelper {

    public static AntiDoubleClickListener antiDoubleClickListener(View.OnClickListener onClickListener) {
        return new AntiDoubleClickListener(onClickListener);
    }
}