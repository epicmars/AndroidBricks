package com.androidpi.app.bricks.base.fragment;

import android.content.Context;

import com.androidpi.app.bricks.common.FileUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class CacheManager {

    private static volatile CacheManager instance;

    private Context context;

    public CacheManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static void init(Context context) {
        getInstance(context).clearWebCache();
    }

    public static CacheManager getInstance(Context context) {
        if (instance == null) {
            synchronized (CacheManager.class) {
                instance = new CacheManager(context);
            }
        }
        return instance;
    }

    public File webCacheDir() {
        return new File(context.getCacheDir().getPath(), "WebViewCache");
    }

    public void clearWebCache() {
        try {
            FileUtils.rm(webCacheDir());
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}