/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidpi.bricks.gallery.lru;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import androidx.appcompat.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidpi.app.bricks.base.activity.BaseActivity;
import com.androidpi.app.bricks.common.AppUtil;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.lru.cache.ImageCache;
import com.androidpi.bricks.gallery.lru.cache.ImageResizer;

import java.util.ArrayList;
import java.util.List;

import layoutbinder.annotations.BindLayout;

@BindLayout(R.layout.activity_image_detail)
public class ImageDetailActivity extends BaseActivity implements OnClickListener {
    public static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE_ENTRIES = "image_entries";
    public static final String EXTRA_CURRENT_ENTRY_POS = "current_entry_pos";

    private ImagePagerAdapter mAdapter;
    private ImageResizer mImageResizer;
    private ViewPager mPager;
    private PopupWindow mPopEdit;
    private TextView mTvEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final List<ImageFileEntry> entries = (ArrayList<ImageFileEntry>) intent
                .getSerializableExtra(EXTRA_IMAGE_ENTRIES);
        final int currentItem = intent.getIntExtra(EXTRA_CURRENT_ENTRY_POS, -1);
        // Set up ViewPager and backing adapter
        // mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
        // Images.imageUrls.length);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), entries);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_page_margin));
        mPager.setOffscreenPageLimit(2);

        mTvEdit = (TextView) findViewById(R.id.tv_edit);

        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        // Enable some additional newer visibility and ActionBar features to
        // create a more
        // immersive photo viewing experience
        if (AppUtil.hasHoneycomb()) {
            final ActionBar actionBar = getSupportActionBar();

            // Hide title text and set home as up
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Hide and show the ActionBar as the visibility changes
            mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int vis) {
                    if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                        actionBar.hide();
                    } else {
                        actionBar.show();
                    }
                }
            });

            // Start low profile mode and hide ActionBar
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }

        // Set the current item based on the extra passed in to this activity

        if (currentItem != -1) {
            mPager.setCurrentItem(currentItem);
        }

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
        final int longest = (height > width ? height : width) / 2;

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
        // app memory

        // The ImageFetcher takes care of loading images into our ImageView
        // children asynchronously
        mImageResizer = new ImageResizer(this, longest);
        mImageResizer.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageResizer.setImageFadeIn(false);

        mTvEdit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageResizer.setExitTasksEarly(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageResizer.setExitTasksEarly(true);
        mImageResizer.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageResizer.closeCache();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageResizer.clearCache();
                Toast.makeText(this, R.string.toast_clear_cache_complete, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.edit_photo:
                editCurrentPhoto();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editCurrentPhoto() {
        // 进入编辑照片页面
        Intent intent = new Intent(this, ImageClippingActivity.class);
        ImageDetailFragment fragment = mAdapter.getItem(mPager.getCurrentItem());
        intent.putExtra(ImageDetailFragment.EXTRA_IMAGE_PATH,
                fragment.getArguments().getString(ImageDetailFragment.EXTRA_IMAGE_PATH));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_images, menu);
        return true;
    }

    /**
     * Called by the ViewPager child fragments to load images via the one
     * ImageFetcher
     */
    public ImageResizer getImageResizer() {
        return mImageResizer;
    }

    /**
     * Set on the ImageView in the ViewPager children fragments, to
     * enable/disable low profile mode when the ImageView is touched.
     */
    @TargetApi(VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imageView) {
            final int vis = mPager.getSystemUiVisibility();
            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        } else if (id == R.id.tv_edit) {
            editCurrentPhoto();
        }
    }

    /**
     * The main adapter that backs the ViewPager. A subclass of
     * FragmentStatePagerAdapter as there could be a large number of items in
     * the ViewPager and we don't want to retain them all in memory at once but
     * create/destroy them on the fly.
     */
    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private List<ImageFileEntry> mEntries;

        public ImagePagerAdapter(FragmentManager fm, List<ImageFileEntry> entries) {
            super(fm);
            mEntries = entries;
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public ImageDetailFragment getItem(int position) {
            return ImageDetailFragment.newInstance(mEntries.get(position));
        }
    }

}
