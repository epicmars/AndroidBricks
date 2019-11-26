package com.androidpi.app.bricks.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.androidpi.app.bricks.base.R;

/**
 * Created by on 2018/12/29
 */
public class SquareToastView extends FrameLayout {

    private ImageView ivIcon;
    private TextView tvMsg;

    public SquareToastView(Context context) {
        this(context, null);
    }

    public SquareToastView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareToastView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.toast_square, this);
        ivIcon = findViewById(R.id.iv_icon);
        tvMsg = findViewById(R.id.tv_msg);
    }

    public void setIcon(@DrawableRes int res) {
        if (ivIcon == null)
            return;
        ivIcon.setImageResource(res);
    }

    public void setMsg(@StringRes int res) {
        if (tvMsg == null)
            return;
        tvMsg.setText(res);
    }

    public void setMsg(String msg) {
        tvMsg.setText(msg);
    }
}
