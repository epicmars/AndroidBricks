package com.androidpi.app.bricks.ui.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.androidpi.app.bricks.common.PermissionUtils;
import com.androidpi.app.bricks.databinding.ActivityMainBinding;
import com.androidpi.bricks.libgifsicle.Gifsicle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.androidpi.app.bricks.R;
import com.androidpi.app.bricks.base.activity.BaseActivity;

import java.io.File;
import java.util.List;

import layoutbinder.annotations.BindLayout;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;
public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {


    public static final int REQ_STORAGE_PERMISSION = 1001;

    @BindLayout(R.layout.activity_main)
    ActivityMainBinding binding;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    GifActivity.Companion.start(getContext());
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PermissionUtils.hasStoragePermission(this)) {
            PermissionUtils.requestPermission(this, REQ_STORAGE_PERMISSION, PermissionUtils.EXTERNAL_STORAGE_PERMS, "需要存储权限以读写文件");
        }

        setSupportActionBar(binding.appToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("click: " + v.toString());
                HtmlActivity.start(getContext(), "http://www.baidu.com");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
