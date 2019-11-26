package com.androidpi.app.bricks;

import android.app.Application;

import com.androidpi.app.bricks.logger.Logger;

/**
 * Created by jastrelax on 2018/7/8.
 */
public class AndroidBricksApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init();
    }
}
