package com.androidpi.app.bricks.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidpi.app.bricks.R;
import com.androidpi.app.bricks.base.activity.BaseActivity;
import com.androidpi.app.bricks.base.fragment.HtmlFragment;
import com.androidpi.app.bricks.databinding.ActivityHtmlBinding;

import layoutbinder.annotations.BindLayout;

public class HtmlActivity extends BaseActivity implements HtmlFragment.HtmlPageListener {

    private static final String EXTRA_TITLE_RES = "HtmlActivity.EXTRA_TITLE_RES";
    private static final String EXTRA_TITLE = "HtmlActivity.EXTRA_TITLE";
    private static final String EXTRA_URL = "HtmlActivity.EXTRA_URL";
    private static final String EXTRA_DATA = "HtmlActivity.EXTRA_DATA";

    @BindLayout(R.layout.activity_html)
    ActivityHtmlBinding binding;

    private String mTitle;

    public static void start(Context context, int titleRes, String url) {
        if (TextUtils.isEmpty(url))
            return;
        Intent starter = new Intent(context, HtmlActivity.class);
        starter.putExtra(EXTRA_TITLE_RES, titleRes);
        starter.putExtra(EXTRA_URL, url);
        context.startActivity(starter);
    }

    public static void start(Context context, String title, String url) {
        if (TextUtils.isEmpty(url))
            return;
        Intent starter = new Intent(context, HtmlActivity.class);
        starter.putExtra(EXTRA_TITLE, title);
        starter.putExtra(EXTRA_URL, url);
        context.startActivity(starter);
    }

    public static void start(Context context, String url) {
        if (TextUtils.isEmpty(url))
            return;
        Intent starter = new Intent(context, HtmlActivity.class);
        starter.putExtra(EXTRA_URL, url);
        context.startActivity(starter);
    }

    public static void startForData(Context context, String title, String data) {
        if (TextUtils.isEmpty(data))
            return;
        Intent starter = new Intent(context, HtmlActivity.class);
        starter.putExtra(EXTRA_TITLE, title);
        starter.putExtra(EXTRA_DATA, data);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mTitle = getIntent().getStringExtra(EXTRA_TITLE);
            int titleRes = getIntent().getIntExtra(EXTRA_TITLE_RES, -1);
            String url = getIntent().getStringExtra(EXTRA_URL);
            String data = getIntent().getStringExtra(EXTRA_DATA);

            if (titleRes != -1) {
                mTitle = getString(titleRes);
            }
            binding.appToolbar21.setTitle(mTitle);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if(url != null)
                ft.replace(R.id.fl_content, HtmlFragment.newInstance(url));
            else if(data != null)
                ft.replace(R.id.fl_content, HtmlFragment.newInstanceForData(data));
            ft.commit();
        }
    }

    @Override
    public void onLoadTitle(String title) {
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = title;
        }
        binding.appToolbar21.setTitle(mTitle);
    }
}