package com.androidpi.app.bricks.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by jastrelax on 2018/6/13.
 */
public class ClearableEditText extends FrameLayout {

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
    }
}
