package com.androidpi.bricks.gallery.plain;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.androidpi.app.bricks.base.databinding.BindData;
import com.androidpi.app.bricks.common.FileUtils;
import com.androidpi.bricks.gallery.BaseViewHolder;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.ViewHolderTakePhotoItemBinding;

@BindData(value = R.layout.view_holder_take_photo_item, dataTypes = TakePhotoItem.class)
public class TakePhotoItemViewHolder extends BaseViewHolder<TakePhotoItem, ViewHolderTakePhotoItemBinding> {

    public static interface OnTakePhotoListener {
        void onTakePhoto(TakePhotoItem TakePhotoItem);
    }

    public static final int REQUEST_CODE_TAKE_PHOTO = 1;

    public static final int REQUEST_CAMERA = 2;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private OnTakePhotoListener takePhotoListener;

    public TakePhotoItemViewHolder(View view) {
        super(view);
        if(context instanceof OnTakePhotoListener) {
            takePhotoListener = (OnTakePhotoListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    " must implement OnTakePhotoListener");
        }
    }

    @Override
    public void render(TakePhotoItem data, int position) {
        if(null == data)
            return;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoWithPermission(data);
            }
        });
    }

    private void takePhotoWithPermission(TakePhotoItem data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (((Activity)context).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePhoto(data);
            } else {
                if (((Activity)context).shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(context, "需要开启相机权限进行拍照", Toast.LENGTH_SHORT).show();
                }
                ((Activity)context).requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else {
            takePhoto(data);
        }
    }

    private void takePhoto(TakePhotoItem data) {
        Intent action = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (((Activity)context).getIntent().resolveActivity(context.getPackageManager()) != null) {
            Uri photoUri = FileUtils.getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            data.setPhotoUri(photoUri);
            takePhotoListener.onTakePhoto(data);
            action.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            ((Activity)context).startActivityForResult(action, REQUEST_CODE_TAKE_PHOTO);
        }
    }
}