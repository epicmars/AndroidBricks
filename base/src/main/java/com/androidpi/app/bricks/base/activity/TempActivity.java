package com.androidpi.app.bricks.base.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidpi.app.bricks.base.R;

/** 模板Activity，用户插入任意的Fragment作为其主内容。 Created by on 2018/11/9 */
public class TempActivity extends BaseActivity {

    private static final String EXTRA_FRAGMENT_CLASSNAME = "TempActivity.EXTRA_FRAGMENT_CLASSNAME";
    private static final String EXTRA_FRAGMENT_ARGUMENTS = "TempActivity.EXTRA_FRAGMENT_ARGUMENTS";

    public static void start(Context context, Class<? extends Fragment> fClass, Bundle args, int flags) {
        Intent starter = new Intent(context, TempActivity.class);
        starter.putExtra(EXTRA_FRAGMENT_CLASSNAME, fClass.getName());
        starter.putExtra(EXTRA_FRAGMENT_ARGUMENTS, args);
        starter.addFlags(flags);
        context.startActivity(starter);
    }

    public static void start(Context context, Class<? extends Fragment> fClass) {
        start(context, fClass, null, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null) {
            String fName = intent.getStringExtra(EXTRA_FRAGMENT_CLASSNAME);
            Bundle args = intent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content, Fragment.instantiate(this, fName, args));
            ft.commit();
        }
    }
}