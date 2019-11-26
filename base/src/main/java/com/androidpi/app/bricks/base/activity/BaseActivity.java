package com.androidpi.app.bricks.base.activity;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.androidpi.app.bricks.base.R;
import com.androidpi.app.bricks.base.utils.ColorUtils;
import com.androidpi.app.bricks.base.utils.ViewHelper;

import jetbooster.JetBooster;
import layoutbinder.LayoutBinder;
import layoutbinder.runtime.LayoutBinding;

public abstract class BaseActivity extends AppCompatActivity {

    protected LayoutBinding layoutBinding;
    private boolean isStatusBarSettled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JetBooster.inject(this);
        layoutBinding = LayoutBinder.bind(this);

        Window window = getWindow();
        View view = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // 明亮主题的状态栏，字体为黑色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!isStatusBarSettled && hasFocus) {
                    // 明亮主题的状态栏，字体为黑色
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Window window = getWindow();
                        View view = window.getDecorView();
                        int statusBarWidth = getResources().getDisplayMetrics().widthPixels;
                        int statusBarHeight = ViewHelper.getStatusBarHeight(getContext());
                        Rect rect = new Rect(0, 0, statusBarWidth, statusBarHeight);
                        ViewHelper.createBitmapFromView(view, window, rect, new ViewHelper.BitmapCreateCallback() {
                            @Override
                            public void onBitmapCreated(Bitmap bitmap) {
                                if (!ColorUtils.isDark(bitmap)) {
                                    view.setSystemUiVisibility(
                                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                                }
                                bitmap.recycle();
                            }
                        });
                    }
                    isStatusBarSettled = true;
                }
            }
        });
    }

    @Override
    public boolean isInMultiWindowMode() {
        return super.isInMultiWindowMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (layoutBinding != null) {
            layoutBinding.unbind();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected FragmentActivity getContext() {
        return this;
    }

    protected int getColorCompat(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getColor(colorRes);
        } else {
            return getResources().getColor(colorRes);
        }
    }

    protected int getThemeRes() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            return getApplicationInfo().theme;
        }
    }

    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        return ViewModelProviders.of(this).get(viewModelClass);
    }

    public void fragmentReplace(@IdRes int id, Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(id, fragment);
        ft.commit();
    }

    public void fragmentReplace(@IdRes int id, Fragment fragment, boolean addToStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(id, fragment);
        if (addToStack) {
            ft.addToBackStack(fragment.getClass().getName());
        }
        ft.commit();
    }

    public void showDialogFragment(DialogFragment fragment) {
        if (fragment == null)
            return;
        if (!fragment.isAdded()) {
            fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
        }
    }

    public void registerLocalReceiver(BroadcastReceiver receiver, String[] actions) {
        registerLocalReceiver(receiver, new IntentFilter(){
            {
                for (String action : actions) {
                    addAction(action);
                }
            }
        });
    }

    public void registerLocalReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, intentFilter);
    }

    public void unregisterLocalReceiver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    private void hide(Fragment f) {
        getSupportFragmentManager().beginTransaction().hide(f).commit();
    }

    private void show(Fragment f) {
        getSupportFragmentManager().beginTransaction().show(f).commit();
    }
}
