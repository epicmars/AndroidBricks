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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidpi.app.bricks.common.AppUtil;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.lru.cache.ImageResizer;
import com.androidpi.bricks.gallery.lru.cache.ImageWorker;

/**
 * This fragment will populate the children of the ViewPager from
 * {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment {
    public static final String EXTRA_IMAGE_PATH = "extra_image_path";
    private String mImagePath;
    private ImageView mImageView;
    private ImageResizer mImageResizer;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {
    }

    /**
     * Factory method to generate a new instance of the fragment given an image
     * number.
     *
     * @param entry The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(ImageFileEntry entry) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(EXTRA_IMAGE_PATH, entry.getFile().getPath());
        f.setArguments(args);

        return f;
    }

    /**
     * Populate image using a url from extras, use the convenience factory
     * method {@link ImageDetailFragment} to create this
     * fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImagePath = getArguments() != null ? getArguments().getString(EXTRA_IMAGE_PATH) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.fragment_image_detail, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the
        // ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageDetailActivity.class.isInstance(getActivity())) {
            mImageResizer = ((ImageDetailActivity) getActivity()).getImageResizer();
            mImageResizer.loadImage(mImagePath, mImageView);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && AppUtil.hasHoneycomb()) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }

    public String getImagePath() {
        return mImagePath;
    }
}
