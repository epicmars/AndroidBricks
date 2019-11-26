package com.androidpi.bricks.gallery.lru;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidpi.app.bricks.base.activity.BaseActivity;
import com.androidpi.app.bricks.common.AppUtil;
import com.androidpi.bricks.gallery.BuildConfig;
import com.androidpi.bricks.gallery.DialogFragmentBuilder;
import com.androidpi.bricks.gallery.ImageEntryLoader;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.ActivityLruGalleryBinding;
import com.androidpi.bricks.gallery.lru.cache.ImageCache;
import com.androidpi.bricks.gallery.lru.cache.ImageResizer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import layoutbinder.annotations.BindLayout;
import timber.log.Timber;

public class LruGalleryActivity extends BaseActivity {

    @BindLayout(R.layout.activity_lru_gallery)
    ActivityLruGalleryBinding binding;

    private static final int LOAD_FINSHED = 1;

    private static final String THUMB_CACHE_DIR = "thumbs";
    private static final Pattern IMAGE_FILE_PATTERN = Pattern.compile("^[\\w\\d\\s]+.(png|jpg|gif|bmp)$",
            Pattern.CASE_INSENSITIVE);
    private static final int MIN_IMAGE_SIZE = 300;

    private static final String KEY_IS_ROOTDIR = "isRootDir";
    private static final String KEY_DIR_ENTRY = "dirEntry";

    GridView mGvPhoto;
    private LoadingDialogFragment mWaitingDialog;

    // 当前目录
    private String mCurrentDir;
    private boolean mIsRootDir;
    private Map<String, ImageFileEntry> mImageEntries;
    private boolean mShowDialog = true;
    //
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageResizer mImageResizer;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_FINSHED:
                    if (mWaitingDialog != null)
                        DialogFragmentBuilder.newInstance(LruGalleryActivity.this).dimiss(mWaitingDialog);
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ImageAdapter(this);
        mGvPhoto.setAdapter(mAdapter);
        if (mShowDialog && getIntent().getBooleanExtra(KEY_IS_ROOTDIR, true)) {
            mWaitingDialog = new LoadingDialogFragment();
            mWaitingDialog.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
            Bundle bundle = new Bundle();
            bundle.putString("title", "");
            mWaitingDialog.setArguments(bundle);
            DialogFragmentBuilder.newInstance(this).show(mWaitingDialog);
            mShowDialog = false;
        }
        initiateFields(savedInstanceState);
        initiateUIListeners();
    }

    protected void initiateFields(Bundle savedInstanceState) {

        mImageEntries = new LinkedHashMap<>();
        Intent i = getIntent();
        mIsRootDir = i.getBooleanExtra(KEY_IS_ROOTDIR, true);
        if (!mIsRootDir) {
            // 显示所有具有大图的图像目录
            ImageFileEntry dirEntry = (ImageFileEntry) i.getSerializableExtra(KEY_DIR_ENTRY);
            showImageEntries(dirEntry);
        }

        mCurrentDir = getExternalStorageDirectory();

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, THUMB_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
        // app memory

        // The ImageResizer takes care of loading images into our ImageView
        // children asynchronously
        mImageResizer = new ImageResizer(this, mImageThumbSize);
        mImageResizer.setLoadingImage(R.mipmap.empty_photo);
        mImageResizer.addImageCache(getSupportFragmentManager(), cacheParams);

        //
//		showImageEntries();
        //
        getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<Map<String, ImageFileEntry>>() {

            @Override
            public Loader<Map<String, ImageFileEntry>> onCreateLoader(int id, Bundle args) {
                if (mIsRootDir)
                    return ImageEntryLoader.create(LruGalleryActivity.this);
                else
                    return null;
            }

            @Override
            public void onLoadFinished(Loader<Map<String, ImageFileEntry>> loader, Map<String, ImageFileEntry> data) {
                mImageEntries.putAll(data);
                mAdapter.addAll(mImageEntries.values());
                if (data != null)
                    mHandler.obtainMessage(LOAD_FINSHED).sendToTarget();
            }

            @Override
            public void onLoaderReset(Loader<Map<String, ImageFileEntry>> loader) {

            }
        });
    }

    protected void initiateUIListeners() {
        mGvPhoto.setOnItemClickListener(new OnItemClickListener() {

            @TargetApi(VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIsRootDir = false;
                ImageFileEntry entry = mAdapter.getItem(position);
                if (entry != null) {
                    if (entry.isDirectory()) {
                        Intent i = new Intent(LruGalleryActivity.this, LruGalleryActivity.class);
                        i.putExtra(KEY_IS_ROOTDIR, mIsRootDir);
                        i.putExtra(KEY_DIR_ENTRY, entry);
                        startActivity(i);
                    } else {
                        ArrayList<ImageFileEntry> imageEntries = new ArrayList<ImageFileEntry>();
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            ImageFileEntry imageEntry = mAdapter.getItem(i);
                            if (!imageEntry.isDirectory()) {
                                imageEntries.add(imageEntry);
                            }
                        }
                        final Intent i = new Intent(LruGalleryActivity.this, ImageDetailActivity.class);
                        i.putExtra(ImageDetailActivity.EXTRA_IMAGE_ENTRIES, imageEntries);
                        i.putExtra(ImageDetailActivity.EXTRA_CURRENT_ENTRY_POS, position);
                        if (AppUtil.hasJellyBean()) {
                            // makeThumbnailScaleUpAnimation() looks kind of
                            // ugly here as the loading spinner may
                            // show plus the thumbnail image in GridView is
                            // cropped. so using
                            // makeScaleUpAnimation() instead.
                            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                                    view.getHeight());
                            startActivity(i, options.toBundle());
                        } else {
                            startActivity(i);
                        }
                    }
                }
            }
        });
        mGvPhoto.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help
                    // with performance
                    if (!AppUtil.hasHoneycomb()) {
                        mImageResizer.setPauseWork(true);
                    }
                } else {
                    mImageResizer.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
//				if(mIsRootDir)
//					mScrollY = 100;
//				else
//					mScrollY = 0;
            }
        });

        // This listener is used to get the final width of the GridView and then
        // calculate the
        // number of columns and the width of each column. The width of each
        // column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used
        // to set the height
        // of each view so we get nice square thumbnails.
        mGvPhoto.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math
                            .floor(mGvPhoto.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (mGvPhoto.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                        if (BuildConfig.DEBUG) {
                            Timber.d("onCreateView - numColumns set to " + numColumns);
                        }
                        if (AppUtil.hasJellyBean()) {
                            mGvPhoto.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mGvPhoto.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            }
        });
    }

    private String getExternalStorageDirectory() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || !Environment.isExternalStorageRemovable()) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return null;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mCurrentDir = savedInstanceState.getString("mCurrentDir");
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageResizer.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageResizer.setPauseWork(false);
        mImageResizer.setExitTasksEarly(true);
        mImageResizer.flushCache();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null)
            outState.putString("mCurrentDir", mCurrentDir);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageResizer.closeCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageResizer.clearCache();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//		String rootPath = getExternalStorageDirectory();
//		if(!mIsRootDir){ // 非根目录
//			// 回滚到原位
//			mCurrentDir = rootPath;
//			mAdapter.clear();
//			for(Entry<String, ImageFileEntry> entry : mImageEntries.entrySet()){
//				if(entry.getValue().isDirectory())
//					mAdapter.add(entry.getValue());
//			}
//			mIsRootDir = true;
//		} else {
//			super.onBackPressed();
//		}
        super.onBackPressed();
    }

    private void showImageEntries(ImageFileEntry entry) {
        mCurrentDir = entry.getFile().getPath();
        mAdapter.clear();
        mAdapter.addAll(entry.getChildEntries());
    }

    /**
     * The main adapter that backs the GridView. This is fairly standard except
     * the number of columns in the GridView is used to create a fake top row of
     * empty views as we use a transparent ActionBar and don't want the real top
     * row of images to start off covered by it.
     */
    private class ImageAdapter extends ArrayAdapter<ImageFileEntry> {

        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private int mActionBarHeight = 0;
        private GridView.LayoutParams mItemViewLayoutParams;

        public ImageAdapter(Context context) {
            super(context, 0);
            mContext = context;
            mItemViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                        context.getResources().getDisplayMetrics());
            }
        }

        @Override
        public int getCount() {
            // If columns have yet to be determined, return no items
            if (getNumColumns() == 0) {
                return 0;
            }

            // Size + number of columns for top empty row
            // return Images.imageThumbUrls.length + mNumColumns;
            return super.getCount();
        }

        @Override
        public ImageFileEntry getItem(int position) {
            // return position < mNumColumns ?
            // null : Images.imageThumbUrls[position - mNumColumns];
            return super.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            // return position < mNumColumns ? 0 : position - mNumColumns;
            return super.getItemId(position);
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty
            // views
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            // return (position < mNumColumns) ? 1 : 0;
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // BEGIN_INCLUDE(load_gridview_item)
            // First check if this is the top row
            /*
             * if (position < mNumColumns) { if (convertView == null) {
			 * convertView = new View(mContext); } // Set empty view with height
			 * of ActionBar convertView.setLayoutParams(new
			 * AbsListView.LayoutParams( LayoutParams.MATCH_PARENT,
			 * mActionBarHeight)); return convertView; }
			 */


            // Now handle the main ImageView thumbnails
            ViewHolder holder = null;
            if (convertView == null) { // if it's not recycled, instantiate and
                // initialize
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image_entry, null);
                convertView.setLayoutParams(mItemViewLayoutParams);
                holder.ivImageEntry = (ImageView) convertView.findViewById(R.id.iv_image_entry);
                holder.ivImageEntry.setScaleType(ImageView.ScaleType.CENTER_CROP);

                holder.tvEntryName = (TextView) convertView.findViewById(R.id.tv_entry_name);
                convertView.setTag(holder);
            } else { // Otherwise re-use the converted view
                holder = (ViewHolder) convertView.getTag();
            }

            // Check the height matches our calculated column width
            if (convertView.getLayoutParams().height != mItemHeight) {
                convertView.setLayoutParams(mItemViewLayoutParams);

            }

            // Finally load the image asynchronously into the ImageView, this
            // also takes care of
            // setting a placeholder image while the background thread runs
            final ImageFileEntry entry = getItem(position);
            final String filePath = entry.getFile().getPath();
            int lastSeperator = filePath.lastIndexOf(File.separator);
            // 载入图像
            if (entry.isDirectory()) { // 目录显示名称并载入缩略图
                mImageResizer.loadImage(entry.getFile(), holder.ivImageEntry);
                holder.tvEntryName.setVisibility(View.VISIBLE);
                holder.tvEntryName.setText(filePath.substring(lastSeperator + 1));
            } else {
                mImageResizer.loadImage(entry.getFile().getPath(), holder.ivImageEntry);
                holder.tvEntryName.setVisibility(View.GONE);
            }
            return convertView;
            // END_INCLUDE(load_gridview_item)
        }

        /**
         * Sets the item height. Useful for when we know the column width so the
         * height can be set to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mItemViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageResizer.setImageSize(height);
            notifyDataSetChanged();
        }

        public int getNumColumns() {
            return mNumColumns;
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        private class ViewHolder {
            ImageView ivImageEntry;
            TextView tvEntryName;
        }
    }

}

