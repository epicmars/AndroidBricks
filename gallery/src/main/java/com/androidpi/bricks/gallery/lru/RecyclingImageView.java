/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewConfiguration;

import com.androidpi.bricks.gallery.ViewConfig;
import com.androidpi.bricks.gallery.lru.cache.RecyclingBitmapDrawable;

/**
 * Sub-class of ImageView which automatically notifies the drawable when it is
 * being displayed.
 */
public class RecyclingImageView extends AppCompatImageView {

    private static final String TAG = "RecyclingImageView";

    /*-----------拖放相关域-----------*/
    private int mTouchMode = ViewConfig.TouchMode.TOUCH_MODE_IDLE;
    private int mTouchSlop;
    private int mLongPressTime;

    public RecyclingImageView(Context context) {
        super(context);
        initiate(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initiate(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initiate(context);
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            // The drawable is a CountingBitmapDrawable, so notify it
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }

    private void initiate(Context context) {
        //
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mLongPressTime = ViewConfiguration.getLongPressTimeout();
    }

    /**
     * @see android.widget.ImageView#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        // This has been detached from Window, so clear the drawable
        setImageDrawable(null);

        super.onDetachedFromWindow();
    }

    /**
     * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        // Keep hold of previous Drawable
        final Drawable previousDrawable = getDrawable();

        // Call super to set new Drawable
        super.setImageDrawable(drawable);

        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true);

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

/*	private float mLastX, mLastY; // 上一次触控位置
    private long mDownTime; // 记录点击下的时间

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			Log.d(TAG, "ACTION_DOWN");
			float x = event.getX();
			float y = event.getY();
			if (mTouchMode == TouchMode.TOUCH_MODE_IDLE) {
				// 记录按下时间
				mDownTime = System.currentTimeMillis();
				mTouchMode = TouchMode.TOUCH_MODE_DOWN;
				Log.d(TAG, "TOUCH_MODE_DOWN");
				mLastX = x;
				mLastY = y;
			}
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			Log.d(TAG, "ACTION_MOVE");
			float x = event.getX();
			float y = event.getY();
			if (mTouchMode == TouchMode.TOUCH_MODE_DOWN) {
				long deltaTime = System.currentTimeMillis() - mDownTime;
				// 按下达到长按时间
				if (deltaTime >= mLongPressTime) {
					mTouchMode = TouchMode.TOUCH_MODE_LONG_PRESS;
					Log.d(TAG, "TOUCH_MODE_LONG_PRESS");
					mLastX = x;
					mLastY = y;
				}
			}
			float deltaX = Math.abs(x - mLastX);
			float deltaY = Math.abs(y - mLastY);
			// 长按后开始进入拖放状态
			if (mTouchMode == TouchMode.TOUCH_MODE_LONG_PRESS && (deltaX > mTouchSlop || deltaY > mTouchSlop)) {
				mTouchMode = TouchMode.TOUCH_MODE_DRAGGING;
				Log.d(TAG, "TOUCH_MODE_DRAGGING");
			}
			return false;
		}
		case MotionEvent.ACTION_UP: {
			if (mTouchMode == TouchMode.TOUCH_MODE_DOWN) {
				performClick();
			}
			Log.d(TAG, "ACTION_UP");
			initTouchMode();
			return false;
		}
		case MotionEvent.ACTION_CANCEL: {
			Log.d(TAG, "ACTION_CANCEL");
			initTouchMode();
			return false;
		}
		default:
			break;
		}

		return false;
	}

	private void initTouchMode() {
		mTouchMode = TouchMode.TOUCH_MODE_IDLE;
		Log.d(TAG, "TOUCH_MODE_IDLE");
	}*/

}
