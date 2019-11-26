package com.androidpi.bricks.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.HEIGHT;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.provider.MediaStore.MediaColumns.WIDTH;

public class ImageEntryLoader extends AsyncTaskLoader<Map<String, ImageFileEntry>> {

    private static final int MIN_LENGTH = 300;

    /**
     * Static library support version of the framework's {@link android.content.CursorLoader}.
     * Used to write apps that run on platforms prior to Android 3.0.  When running
     * on Android 3.0 or above, this implementation is still used; it does not try
     * to switch to the framework's implementation.  See the framework SDK
     * documentation for a class overview.
     */
    final ForceLoadContentObserver mObserver;

    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
//	    boolean mContentChanged;

    Cursor mCursor;
    Cursor oldCursor;
    Map<String, ImageFileEntry> mImageEntries;

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public ImageEntryLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public ImageEntryLoader(Context context, Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
        mImageEntries = new LinkedHashMap<>();
    }

    /**
     * todo 使用builder模式
     *
     * @param context
     * @return
     */
    public static ImageEntryLoader create(Context context) {
        Uri imageEntryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{_ID, TITLE, DISPLAY_NAME, DATE_ADDED, WIDTH, HEIGHT, DATA};
        StringBuilder selection = new StringBuilder();
        selection.append(WIDTH).append(" > ? ").append(" and ").append(HEIGHT).append(" > ? ");
        String[] selectionArgs = new String[]{String.valueOf(MIN_LENGTH), String.valueOf(MIN_LENGTH)};
        return new ImageEntryLoader(context, imageEntryUri, projection, selection.toString(), selectionArgs, null);
    }

    /* Runs on a worker thread */
    @Override
    public Map<String, ImageFileEntry> loadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder);
        if (cursor != null) {
            mCursor = cursor;
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(_ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
            long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File file = new File(filePath);
            if (!file.exists())
                continue;
            ImageFileEntry entry = new ImageFileEntry(title, dateAdded, displayName, width, height, file);
            cacheImageEntryTree(entry);
            Timber.d("id: %d, title: %s, dateAdded: %d, displayName %s, width: %d, height: %d, %s", id, title, dateAdded, displayName, width, height, filePath);
        }
        return mImageEntries;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Map<String, ImageFileEntry> entries) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (mCursor != null) {
                mCursor.close();
            }
            return;
        }

        if (isStarted()) {
            super.deliverResult(entries);
        }

        oldCursor = mCursor;
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mImageEntries);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Map<String, ImageFileEntry> data) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    /**
     * 缓存图像文件夹入口，并组织入口结构，这里只是将具有图像的文件夹入口缓存到了Map中，
     * 并将其中的图像文件入口存到了其子入口集合中。
     *
     * @param imageFileEntry 一个图像文件入口。
     */
    private void cacheImageEntryTree(ImageFileEntry imageFileEntry) {
        File parent = imageFileEntry.getFile().getParentFile();
        if (parent.exists() && parent.isDirectory()) {
            if (!mImageEntries.containsKey(parent.getPath())) {
                ImageFileEntry parentEntry = new ImageFileEntry();
                parentEntry.setFile(parent);
                parentEntry.addChildEntry(imageFileEntry);
                //
                mImageEntries.put(parent.getPath(), parentEntry);
            } else {
                ImageFileEntry parentEntry = mImageEntries.get(parent.getPath());
                parentEntry.addChildEntry(imageFileEntry);
            }
        }
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(mSortOrder);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
//	        writer.print(prefix); writer.print("mContentChanged="); writer.println(mContentChanged);
    }

}