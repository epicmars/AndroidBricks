package com.androidpi.app.bricks.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.IntDef;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SlideLayout extends ViewGroup {

    /**
     * 表示slide处于idle状态，没有发生滑动
     */
    public static final int STATE_IDLE = ViewConfig.TouchMode.TOUCH_MODE_IDLE;
    /**
     * 表示slide处于按下动作，即表示触控操作的开始
     */
    public static final int STATE_DOWN = ViewConfig.TouchMode.TOUCH_MODE_DOWN;
    /**
     * 表示slide处于长按状态
     */
    public static final int STATE_LONG_PRESS = ViewConfig.TouchMode.TOUCH_MODE_LONG_PRESS;
    /**
     * 表示slide正被用户拖拽
     */
    public static final int STATE_DRAGGING = ViewConfig.TouchMode.TOUCH_MODE_DRAGGING;
    /**
     * 表示slide正设置到一个最终的位置，如向左滑动距离超过右边slide的一半后，释放动作，那么右边slide将会被完全打开
     */
    public static final int STATE_SETTLING = ViewConfig.TouchMode.TOUCH_MODE_SETTLING;
    // attr
    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.layout_gravity};
    private static final Interpolator sInterpolator = new Interpolator() {

        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    //
    private ScrollerCompat mScroller;
    private int mTouchState = STATE_IDLE;
    private int mTouchSlop;
    private long mLongPressTime;
    private float mLastX, mLastY;
    //	private long mElaspedTime;
    private float mTouchX, mTouchY;
    /**
     * 偏移范围，当前偏移量不能超出此范围
     */
    private int[] mOffsetRange = new int[2];
    /**
     * 当前偏移百分比，即偏移量占偏移范围宽度的比例
     */
    private float mCurrentOffset;

    public SlideLayout(Context context) {
        this(context, null);
    }


    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = ScrollerCompat.create(context, sInterpolator);
        mTouchSlop = ViewConfig.getScaledTouchSlop(context);
        mLongPressTime = ViewConfig.getLongPressTimeout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // onMeasure中的参数
        // 仅支持横向布局，第一个布局为主布局，第二个布局为滑动布局
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 测量当前布局

        // 测量子视图
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            // 判断子视图是否需要测量
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (isContentView(child)) {
                int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize - p.leftMargin - p.rightMargin,
                        MeasureSpec.EXACTLY);
                int contentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize - p.topMargin - p.bottomMargin,
                        MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else {
                int slideWidthSpec = getChildMeasureSpec(widthMeasureSpec, p.leftMargin + p.rightMargin, p.width);
                int slideHeightSpec = getChildMeasureSpec(heightMeasureSpec, p.topMargin + p.bottomMargin, p.height);
                child.measure(slideWidthSpec, slideHeightSpec);
                widthSize += child.getMeasuredWidth() + p.leftMargin + p.rightMargin; // margin属于子视图布局属性，不属于其自身大小的一部分
                mOffsetRange[0] = -child.getMeasuredWidth() - p.leftMargin - p.rightMargin;
            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 测量子视图
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            // 判断子视图是否需要测量
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final LayoutParams p = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                final int left = l + p.leftMargin;
                final int top = t + p.topMargin;
                final int right = left + child.getMeasuredWidth();
                final int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
            } else {
                if (p.gravity == Gravity.RIGHT) {
                    final int left = r - child.getMeasuredWidth() - p.rightMargin;
                    final int top = t + p.topMargin;
                    final int right = left + child.getMeasuredWidth();
                    final int bottom = top + child.getMeasuredHeight();
                    child.layout(left, top, right, bottom);
                }
                // TODO 考虑垂直方向上的Gravity
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean interceptDrag = false;
        int action = MotionEventCompat.getActionMasked(ev); // 不带pointerId的action
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = ev.getX();
                mTouchY = ev.getY();
                if (mTouchState == STATE_IDLE) {
                    mTouchState = STATE_DOWN;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final float dx = x - mTouchX;
                if (mTouchState == STATE_DOWN) {
                    if (Math.abs(dx) > mTouchSlop) {
                        mTouchState = STATE_DRAGGING;
                    }
                }
                mTouchX = x;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                break;
        }
        interceptDrag = (mTouchState == STATE_DRAGGING);
        return interceptDrag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean interestEvent = false;
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mScroller.abortAnimation();
                return true;
            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                if (mTouchState == STATE_DRAGGING) {
                    interestEvent = true;
                    final float dx = x - mTouchX; // ?此处使用mLastX产生抖动
                    offsetHorizental(dx);
/*				int nextOffset = (int) (mCurrentOffset + dx); // 此处代码过于冗长，需要判断偏移量是否处于固定范围，可以直接使用max，min限定范围
                if(isOffsetInRange(nextOffset)){
					offsetLeftAndRight((int)dx); // 此处存在浮点转整形的精度丢失，精度丢失累加，偏移量错误源于此处
					mCurrentOffset += dx;
				} else {
					int canOffset = canOffset((int) dx);
					offsetLeftAndRight(canOffset);
					mCurrentOffset += canOffset;
				}*/
/*				if(Math.abs(dx) > mTouchSlop){ // 如果已经处于STATE_DRAGGIND状态，不再需要作此判断，会造成滑动的不平滑
                    offsetLeftAndRight((int)dx);
				}*/
                }
                mLastX = x;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                final float left = getLeft();
                mCurrentOffset = Math.abs(left / (mOffsetRange[0] - mOffsetRange[1]));
                if (mCurrentOffset > 0.5f) { // 打开
                    // duration的计算可以使用速度追踪方法
                    mScroller.startScroll((int) left, 0, (int) (mOffsetRange[0] - left), 0);
                } else { // 关闭
                    mScroller.startScroll((int) left, 0, (int) (mOffsetRange[1] - left), 0);
                }
                mTouchState = STATE_IDLE;
                break;

        }

        return interestEvent;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int oldLeft = getLeft();
            final int newLeft = mScroller.getCurrX();
            offsetLeftAndRight(newLeft - oldLeft);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 关闭
     */
    public void close() {
        final float left = getLeft();
        mScroller.startScroll((int) left, 0, (int) (mOffsetRange[1] - left), 0);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        // TODO Auto-generated method stub
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    /**
     * 子视图是否是内容视图
     *
     * @param child 子视图
     * @return
     */
    private boolean isContentView(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return lp.gravity == Gravity.NO_GRAVITY;
    }

    /**
     * 水平偏移，直接以布局边界作为当前偏移量
     *
     * @return
     */
    private void offsetHorizental(float dx) {
        int oldLeft = getLeft();
        int newLeft = (int) (oldLeft + dx);
        int clampX = Math.max(mOffsetRange[0], Math.min(newLeft, mOffsetRange[1]));
        offsetLeftAndRight(clampX - oldLeft);
    }

/*	
     * 当前偏移量是否处于偏移范围内
	 * @param currentOffset当前偏移量，主要用于对mCurrentOffset的判断 
	 * @return
	private boolean isOffsetInRange(int currentOffset){
		return currentOffset >= mOffsetRange[0] && currentOffset <= mOffsetRange[1];
	}
	

	 * 返回可以移动的偏移量
	 * @param offset 需要偏移的量
	 * @return
	private int canOffset(int offset){
		int canOffset = 0;
		int nextOffset = mCurrentOffset + offset;
		if(!isOffsetInRange(nextOffset)){
			if(nextOffset < mOffsetRange[0]){
				canOffset = mOffsetRange[0] - mCurrentOffset;
			} else if(nextOffset > mOffsetRange[1]){
				canOffset = mOffsetRange[1] - mCurrentOffset;
			}
		}
		return canOffset;
	}*/

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    //

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? new LayoutParams((LayoutParams) p)
                : p instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @IntDef({STATE_IDLE, STATE_DOWN, STATE_LONG_PRESS, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    private static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity = Gravity.NO_GRAVITY;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(LayoutParams p) {
            super(p);
            gravity = p.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

}