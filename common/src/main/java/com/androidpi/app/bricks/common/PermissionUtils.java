package com.androidpi.app.bricks.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class PermissionUtils {

    public static final int REQ_STORAGE_PERMISSION = 1001;

    public static final String[] EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    public static boolean hasStoragePermission(Context context) {
        return EasyPermissions.hasPermissions(context, EXTERNAL_STORAGE_PERMS);
    }

    public static void requestPermission(Fragment fragment, int reqCode, String[] perms, String rationale) {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(fragment, reqCode, perms)
                .setRationale(rationale)
                .build());
    }

    public static void requestPermission(Activity activity, int reqCode, String[] perms, String rationale) {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(activity, reqCode, perms)
                .setRationale(rationale)
                .build());
    }

    public static void requestStoragePermission(Activity activity) {
        requestPermission(activity, REQ_STORAGE_PERMISSION, EXTERNAL_STORAGE_PERMS, "需要存储权限以读写文件");
    }
}