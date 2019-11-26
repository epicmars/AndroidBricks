package com.androidpi.app.bricks.utils;

import android.view.View;

public class AntiDoubleClickListener implements View.OnClickListener {

    View.OnClickListener listener;
    private long lastClickTime = 0L;
    private static final long DOUBLE_CLICK_THRESHOLD = 1500L;

    public AntiDoubleClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        // Double click guard.
        long current = System.currentTimeMillis();
        if (current - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
            return;
        }

        lastClickTime = current;

        if (listener != null) {
            listener.onClick(v);
        }
    }
}