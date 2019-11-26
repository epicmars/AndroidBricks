package com.androidpi.app.bricks.widgets;

import android.content.Context;
import android.view.ViewConfiguration;

public class ViewConfig {

    /**
     * 返回认为滑动开始前所移动的距离
     *
     * @return
     */
    public static int getScaledTouchSlop(Context context) {
        return ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 按下变为长按的时间(微秒)
     *
     * @return
     */
    public static int getLongPressTimeout() {
        return ViewConfiguration.getLongPressTimeout();
    }

    /**
     * 触控模式定义
     */
    public class TouchMode {
        public static final int TOUCH_MODE_IDLE = 0; // 未发生触控或触控结束
        public static final int TOUCH_MODE_DOWN = 1; // 按下
        public static final int TOUCH_MODE_LONG_PRESS = 2; // 长按
        public static final int TOUCH_MODE_DRAGGING = 3; // 拖拽
        public static final int TOUCH_MODE_SETTLING = 4; // 释放并返回一个最终位置
    }
}