package com.androidpi.bricks.gallery.plain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.androidpi.app.bricks.base.activity.BaseActivity;
import com.androidpi.app.bricks.common.ImageUtil;
import com.androidpi.bricks.gallery.ImageEntryLoader;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.ActivityGalleryBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import layoutbinder.annotations.BindLayout;
import timber.log.Timber;

public class GalleryActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Map<String, ImageFileEntry>>,
        ImageFileEntryViewHolder.OnImageFileEntryOperationListener,
        Handler.Callback, TakePhotoItemViewHolder.OnTakePhotoListener {

    @BindLayout(R.layout.activity_gallery)
    ActivityGalleryBinding binding;

    private static final String EXTRAS_CURRENT_IMAGE_FILE_ENTRY = "com.meishikr.app.view.activity.gallery.EXTRAS_CURRENT_IMAGE_FILE_ENTRY";

    private static final String TAG_GALLERY = "tag_gallery";

    private static final int WHAT_IMAGE_LOAD_FINSHED = 1;

    private ImageFileEntry currentEntry;

    private Handler handler;

    private GalleryFragment galleryFragment;

    private SortedMap<Integer, ImageFileEntry> selectedImages;

    private int selectedIndex = 1;

    private Uri photoUri;

    public static void launch(Context context, ImageFileEntry entry) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra(EXTRAS_CURRENT_IMAGE_FILE_ENTRY, entry);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(binding.toolbar);
        selectedImages = new TreeMap<>();
        currentEntry = getIntent().getParcelableExtra(EXTRAS_CURRENT_IMAGE_FILE_ENTRY);
        handler = new Handler(this);
        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TakePhotoItemViewHolder.REQUEST_CODE_TAKE_PHOTO == requestCode) {
            if (RESULT_OK == resultCode) {
                File photo;
                if (null != data) {
                    photo = new File(data.getData().getPath());
                    Timber.d("" + data.getData());
                } else {
                    photo = new File(photoUri.getPath());
                }
                // 扫描媒体文件，通知系统媒体存储更新照片信息
                addPhotoToGallery(photo.getPath());
                //
                ImageFileEntry fileEntry = new ImageFileEntry();
                fileEntry.setFile(photo);
                selectImageFile(fileEntry);
                // 压缩并保存文件到应用缓存
                setResultIntent();
            } else if (RESULT_CANCELED == resultCode) {
                // 用户取消拍摄
            } else {
                // 拍照失败
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 压缩并保存图像到应用图像文件夹。
     * @param entry
     */
    private void compressAndSave(ImageFileEntry entry) {
        File saveDir = getExternalFilesDir("photoPaths");
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File savePhoto = new File(saveDir, entry.getFile().getName());
        Bitmap bitmap = ImageUtil.resizeImage(entry.getFile().getPath());
        ImageUtil.saveBitmap(savePhoto.getPath(), bitmap);
        entry.setFile(savePhoto);
    }

    private void addPhotoToGallery(String imagePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(imagePath));
        intent.setData(uri);
        sendBroadcast(intent);
    }

    @Override
    public Loader<Map<String, ImageFileEntry>> onCreateLoader(int id, Bundle args) {
        return ImageEntryLoader.create(this);
    }

    @Override
    public void onLoadFinished(Loader<Map<String, ImageFileEntry>> loader, Map<String, ImageFileEntry> data) {
        if(null == currentEntry) {
            // xxx http://stackoverflow.com/questions/22788684/can-not-perform-this-action-inside-of-onloadfinished
            // xxx https://groups.google.com/forum/#!topic/android-developers/dXZZjhRjkMk/discussion
            Message message = handler.obtainMessage(WHAT_IMAGE_LOAD_FINSHED);
            Bundle b = new Bundle();
            b.putParcelableArrayList(GalleryFragment.ARG_IMAGE_FILE_ENTRIES, new ArrayList<>(data.values()));
            message.setData(b);
            message.sendToTarget();
        }
    }

    @Override
    public void onLoaderReset(Loader<Map<String, ImageFileEntry>> loader) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_IMAGE_LOAD_FINSHED: {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                galleryFragment = GalleryFragment.newInstance(msg.getData().getParcelableArrayList(GalleryFragment.ARG_IMAGE_FILE_ENTRIES));
                ft.replace(R.id.content, galleryFragment, TAG_GALLERY);
                ft.commit();
            }
        }
        return false;
    }

    @Override
    public void onImageFileEntryClick(ImageFileEntry entry) {
        if(entry.isDirectory()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ArrayList<ImageFileEntry> entries = (ArrayList<ImageFileEntry>) entry.getChildEntries();
            Collections.sort(entries, ((o1, o2) -> (int) (o2.getDateAdded() - o1.getDateAdded())));
            galleryFragment = GalleryFragment.newInstance(entries);
            ft.replace(R.id.content, galleryFragment , TAG_GALLERY);
            ft.addToBackStack(null);
            ft.commit();
//            galleryFragment.updateImageFileEntries();
        } else {
            //
        }
    }

    @Override
    public void onImageFileEntryCheck(ImageFileEntry entry, boolean isChecked) {
        if(isChecked) {
            selectImageFile(entry);
            // todo 这里会出现递归式的压缩
            setResultIntent();
        } else {
            removeImageFile(entry);
            setResultIntent();
        }
    }

    private void setResultIntent() {
        // 压缩并保存文件到应用缓存
        for(ImageFileEntry imageFileEntry : selectedImages.values()) {
            compressAndSave(imageFileEntry);
        }
        //
        if(!selectedImages.isEmpty()) {
            Intent intent = new Intent();
            ArrayList<ImageFileEntry> entries = new ArrayList<>(selectedImages.values());
            setResult(RESULT_OK, intent);
        }
    }

    /**
     * 从选择图像文件集合中移除。
     * @param entry
     */
    private void removeImageFile(ImageFileEntry entry) {
        selectedImages.remove(entry.getSelectIndex());
        entry.clearSelectIndex();
    }

    /**
     * 添加到选择图像文件集合中。
     * @param entry
     */
    private void selectImageFile(ImageFileEntry entry) {
        entry.setSelectIndex(selectedIndex);
        selectedImages.put(selectedIndex, entry);
        selectedIndex++;
    }

    @Override
    public void onTakePhoto(TakePhotoItem takePhotoItem) {
        this.photoUri = takePhotoItem.getPhotoUri();
    }

    private boolean isRootDir() {
        if (null != currentEntry) {
            return true;
        }
        return false;
    }
}
