package com.androidpi.app.bricks.logger;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import timber.log.Timber;

/**
 * Created by jastrelax on 2018/7/8.
 */
public class Logger {

    public static void init() {
        Timber.plant(new Timber.DebugTree());
    }

    public static void log(View view) {
        if (view instanceof TextView) {
            Toast.makeText(view.getContext(), ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), view.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
