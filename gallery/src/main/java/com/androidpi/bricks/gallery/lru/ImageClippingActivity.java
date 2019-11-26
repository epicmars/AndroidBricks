package com.androidpi.bricks.gallery.lru;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.androidpi.app.bricks.base.activity.BaseActivity;
import com.androidpi.app.bricks.common.FileUtils;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.ActivityImageClippingBinding;
import com.androidpi.bricks.gallery.lru.cache.ImageCache;
import com.androidpi.bricks.gallery.lru.cache.ImageResizer;
import com.androidpi.bricks.gallery.lru.cache.ImageWorker;

public class ImageClippingActivity extends BaseActivity {

    ActivityImageClippingBinding binding;

    private String mImagePath;
    private ImageResizer mImageResizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_clipping);
        // Fetch screen height and width, to use as our max size when loading
        // images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our
        // images. As the
        // image scaling ensures the image is larger than this, we should be
        // left with a
        // resolution that is appropriate for both portrait and landscape. For
        // best image quality
        // we shouldn't divide by 2, but this will use more memory and require a
        // larger memory
        // cache.
        final int longest = height > width ? height : width;

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this,
                ImageDetailActivity.IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
        // app memory

        // The ImageFetcher takes care of loading images into our ImageView
        // children asynchronously
        mImageResizer = new ImageResizer(this, longest);
        mImageResizer.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageResizer.setImageFadeIn(false);

        binding.ivImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleClipBtns();
            }
        });

        binding.tvClipImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                binding.ivImage.clipImage();
            }
        });

        binding.tvSaveImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String savedImgPath = binding.ivImage.saveClippedImage();
                String path = FileUtils.getAppImageDir(ImageClippingActivity.this).getAbsolutePath() + "/photo_";
                // 图像复制到本地
                FileUtils.copyFile(savedImgPath, path);
                finish();
            }
        });
    }

    private void toggleClipBtns() {
        if (binding.llClipBtns.getVisibility() == View.GONE) {
            TranslateAnimation a = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                    0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f);
            a.setDuration(300);
            binding.llClipBtns.startAnimation(a);
            binding.llClipBtns.setVisibility(View.VISIBLE);
        } else {
            TranslateAnimation a = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                    0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f);
            a.setDuration(300);
            binding.llClipBtns.startAnimation(a);
            binding.llClipBtns.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 载入图像
        Intent i = getIntent();
        mImagePath = i.getStringExtra(ImageDetailFragment.EXTRA_IMAGE_PATH);
        binding.ivImage.setTag(mImagePath);
        mImageResizer.loadImage(mImagePath, binding.ivImage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (binding.ivImage != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(binding.ivImage);
            binding.ivImage.setImageDrawable(null);
        }
    }

}
