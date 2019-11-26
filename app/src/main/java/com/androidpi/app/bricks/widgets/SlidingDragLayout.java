package com.androidpi.app.bricks.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.SystemClock;
import androidx.annotation.IntDef;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("NewApi")
public class SlidingDragLayout extends ViewGroup {

    /**
     * Indicates that any drawers are in an idle, settled state. No animation is
     * in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;
    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;
    /**
     * Indicates that a drawer is in the process of settling to a final
     * position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;
    /**
     * The drawer is unlocked.
     */
    public static final int LOCK_MODE_UNLOCKED = 0;
    /**
     * The drawer is locked closed. The user may not open it, though the app may
     * open it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;
    /**
     * The drawer is locked open. The user may not close it, though the app may
     * close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;
    /**
     * Experimental feature.
     */
    private static final boolean ALLOW_EDGE_LOCK = false;
    private static final int MIN_DRAWER_MARGIN = 64; // dp
    /**
     * Length of time to delay before peeking the drawer.
     */
    private static final int PEEK_DELAY = 160; // ms
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second
    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;
    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.layout_gravity};
    private final ViewDragHelper mDragger;
    private final ViewDragCallback mCallback;
    private int mMinDrawerMargin;
    private int mLockMode;
    private boolean mDisallowInterceptRequested;
    private boolean mChildrenCanceledTouch;
    private DrawerListener mListener;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mDrawerState;
    private boolean mInLayout;
    private boolean mFirstLayout = true;

    public SlidingDragLayout(Context context) {
        this(context, null, 0);
    }

    // private View mContentView;
    // private View mRightDrawer;

    public SlidingDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        final float density = getResources().getDisplayMetrics().density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);
        final float minVel = MIN_FLING_VELOCITY * density;

        mCallback = new ViewDragCallback();

        mDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mCallback);
//		mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
        mDragger.setMinVelocity(minVel);
        mCallback.setDragger(mDragger);

    }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur
     * @see DrawerListener
     */
    public void setDrawerListener(DrawerListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    private boolean isContentView(View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    public View findContentView() {
        return getChildAt(0);
    }

    void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
        if (mListener != null) {
            mListener.onDrawerSlide(drawerView, slideOffset);
        }
    }

    void setDrawerViewOffset(View drawerView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }

        lp.onScreen = slideOffset;
        dispatchOnDrawerSlide(drawerView, slideOffset);
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if
                // specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightMode = MeasureSpec.EXACTLY;
                } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.EXACTLY;
                    heightSize = 300;
                }
            } else {
                throw new IllegalArgumentException("DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }
        setMeasuredDimension(widthSize, heightSize);

        // 测量子视图
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            // 检查是否可见
            if (child.getVisibility() == GONE) {
                continue;
            }
            // 考虑布局参数
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                // 内容View与父布局尺寸相同
                int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize - lp.leftMargin - lp.rightMargin,
                        MeasureSpec.EXACTLY);
                int contentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin,
                        MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else {
                throw new IllegalStateException("Child " + child + " at index " + i
                        + " does not have a valid layout_gravity - must be Gravity.NO_GRAVITY");
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        final int childCount = getChildCount();
        final int width = r - l;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                // child.layout(l + lp.leftMargin, t + lp.topMargin, r -
                // lp.rightMargin, b - lp.bottomMargin);
                child.layout(lp.leftMargin, lp.topMargin, lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            }

        }

        mInLayout = false;
        mFirstLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void computeScroll() {

        // "|" used on purpose; both need to run.
        if (mDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        // "|" used deliberately here; both methods should be invoked.
        final boolean interceptForDrag = mDragger.shouldInterceptTouchEvent(ev);

        boolean interceptForTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                // if (mScrimOpacity > 0) {
                // final View child = mLeftDragger.findTopChildUnder((int) x, (int)
                // y);
                // if (child != null && isContentView(child)) {
                // interceptForTap = true;
                // }
                // }
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // If we cross the touch slop, don't perform the delayed peek for an
                // edge touch.
                if (mDragger.checkTouchSlop(ViewDragHelper.DIRECTION_ALL)) {
                    mCallback.removeCallbacks();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // closeDrawers(true);
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
            }
        }

        return interceptForDrag || interceptForTap || hasPeekingDrawer() || mChildrenCanceledTouch;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragger.processTouchEvent(ev);

        final int action = ev.getAction();
        boolean wantTouchEvents = true;

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = ev.getX();
                final float y = ev.getY();
                boolean peekingOnly = true;
                final View touchedView = mDragger.findTopChildUnder((int) x, (int) y);
                if (touchedView != null && isContentView(touchedView)) {
                    final float dx = x - mInitialMotionX;
                    final float dy = y - mInitialMotionY;
                    final int slop = mDragger.getTouchSlop();

                }
                mDisallowInterceptRequested = false;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mDisallowInterceptRequested = false;
                mChildrenCanceledTouch = false;
                break;
            }
        }

        return wantTouchEvents;
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    public void closeDrawers() {
        closeDrawers(false);
    }

    void closeDrawers(boolean peekingOnly) {
        boolean needsInvalidate = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final int childWidth = child.getWidth();

            needsInvalidate |= mDragger.smoothSlideViewTo(child, getWidth(), child.getTop());

            lp.isPeeking = false;
        }

        mCallback.removeCallbacks();

        if (needsInvalidate) {
            invalidate();
        }
    }

    /**
     * Open the specified drawer view by animating it into view.
     *
     * @param drawerView Drawer view to open
     */
    public void openDrawer(View drawerView) {

        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 1.f;
            lp.knownOpen = true;

            updateChildrenImportantForAccessibility(drawerView, true);
        }
        invalidate();
    }

    /**
     * Close the specified drawer view by animating it into view.
     *
     * @param drawerView Drawer view to close
     */
    public void closeDrawer(View drawerView) {
        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 0.f;
            lp.knownOpen = false;
        } else {
            mDragger.smoothSlideViewTo(drawerView, getWidth(), drawerView.getTop());
        }
        invalidate();
    }

	/*---------------------------------------------------------*/

    /**
     * Check if the given drawer view is currently in an open state. To be
     * considered "open" the drawer must have settled into its fully visible
     * state. To check for partial visibility use
     * {@link #isDrawerVisible(android.view.View)}.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer view is in an open state
     * @see #isDrawerVisible(android.view.View)
     */
    public boolean isDrawerOpen(View drawer) {
        return ((LayoutParams) drawer.getLayoutParams()).knownOpen;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere
     * inbetween.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer is visible on-screen
     * @see #isDrawerOpen(android.view.View)
     */
    public boolean isDrawerVisible(View drawer) {
        return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
    }

    private boolean hasPeekingDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.isPeeking) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve the shared state of all drawers from the component
     * ViewDragHelpers. Should be called whenever a ViewDragHelper's state
     * changes.
     */
    void updateDrawerState(@State int activeState, View activeDrawer) {
        final int state = mDragger.getViewDragState();

        if (activeDrawer != null && activeState == STATE_IDLE) {
            final LayoutParams lp = (LayoutParams) activeDrawer.getLayoutParams();
            if (lp.onScreen == 0) {
                dispatchOnDrawerClosed(activeDrawer);
            } else if (lp.onScreen == 1) {
                dispatchOnDrawerOpened(activeDrawer);
            }
        }

        if (state != mDrawerState) {
            mDrawerState = state;

            if (mListener != null) {
                mListener.onDrawerStateChanged(state);
            }
        }
    }

    void dispatchOnDrawerClosed(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (lp.knownOpen) {
            lp.knownOpen = false;
            if (mListener != null) {
                mListener.onDrawerClosed(drawerView);
            }

            updateChildrenImportantForAccessibility(drawerView, false);

            // Only send WINDOW_STATE_CHANGE if the host has window focus. This
            // may change if support for multiple foreground windows (e.g. IME)
            // improves.
            if (hasWindowFocus()) {
                final View rootView = getRootView();
                if (rootView != null) {
                    rootView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                }
            }
        }
    }

    void dispatchOnDrawerOpened(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (!lp.knownOpen) {
            lp.knownOpen = true;
            if (mListener != null) {
                mListener.onDrawerOpened(drawerView);
            }

            updateChildrenImportantForAccessibility(drawerView, true);

            // Only send WINDOW_STATE_CHANGE if the host has window focus.
            if (hasWindowFocus()) {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            }

            drawerView.requestFocus();
        }
    }

    void cancelChildViewTouch() {
        // Cancel child touches
        if (!mChildrenCanceledTouch) {
            final long now = SystemClock.uptimeMillis();
            final MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).dispatchTouchEvent(cancelEvent);
            }
            cancelEvent.recycle();
            mChildrenCanceledTouch = true;
        }
    }

    private void updateChildrenImportantForAccessibility(View drawerView, boolean isDrawerOpen) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (!isDrawerOpen || isDrawerOpen && child == drawerView) {
                // Drawer is closed and this is a content view or this is an
                // open drawer view, so it should be visible.
                ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            } else {
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? new LayoutParams((LayoutParams) p)
                : p instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @IntDef({STATE_IDLE, STATE_DRAGGING, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    /**
     * @hide
     */
    @IntDef({LOCK_MODE_UNLOCKED, LOCK_MODE_LOCKED_CLOSED, LOCK_MODE_LOCKED_OPEN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LockMode {
    }

    /**
     * @hide
     */
    @IntDef({Gravity.LEFT, Gravity.RIGHT, GravityCompat.START, GravityCompat.END})
    @Retention(RetentionPolicy.SOURCE)
    private @interface EdgeGravity {
    }

    /**
     * Listener for monitoring events about drawers.
     */
    public interface DrawerListener {
        /**
         * Called when a drawer's position changes.
         *
         * @param drawerView  The child view that was moved
         * @param slideOffset The new offset of this drawer within its range, from 0-1
         */
        public void onDrawerSlide(View drawerView, float slideOffset);

        /**
         * Called when a drawer has settled in a completely open state. The
         * drawer is interactive at this point.
         *
         * @param drawerView Drawer view that is now open
         */
        public void onDrawerOpened(View drawerView);

        /**
         * Called when a drawer has settled in a completely closed state.
         *
         * @param drawerView Drawer view that is now closed
         */
        public void onDrawerClosed(View drawerView);

        /**
         * Called when the drawer motion state changes. The new state will be
         * one of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or
         * {@link #STATE_SETTLING}.
         *
         * @param newState The new drawer motion state
         */
        public void onDrawerStateChanged(@State int newState);
    }

    public class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity = Gravity.NO_GRAVITY;
        float onScreen;
        boolean isPeeking;
        boolean knownOpen;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        private final Runnable mPeekRunnable = new Runnable() {
            @Override
            public void run() {
                // peekDrawer();
            }
        };
        private ViewDragHelper mDragger;

        public ViewDragCallback() {

        }

        public ViewDragCallback(int gravity) {

        }

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        public void removeCallbacks() {
            SlidingDragLayout.this.removeCallbacks(mPeekRunnable);
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // Only capture views where the gravity matches what we're looking
            // for.
            // This lets us use two ViewDragHelpers, one for each side drawer.
            return true;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            updateDrawerState(state, mDragger.getCapturedView());
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float offset;
            final int childWidth = changedView.getWidth();

            // This reverses the positioning shown in onLayout.
            final int width = getWidth();
            offset = (float) (width - left) / childWidth;
            setDrawerViewOffset(changedView, offset);

            changedView.setVisibility(offset == 0 ? INVISIBLE : VISIBLE);
            invalidate();
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            final LayoutParams lp = (LayoutParams) capturedChild.getLayoutParams();
            lp.isPeeking = false;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // Offset is how open the drawer is, therefore left/right values
            // are reversed from one another.
            /*
             * final float offset = getDrawerViewOffset(releasedChild); final
			 * int childWidth = releasedChild.getWidth();
			 * 
			 * int left; final int width = getWidth(); left = xvel < 0 || xvel
			 * == 0 && offset > 0.5f ? width - childWidth : width;
			 * 
			 * mDragger.settleCapturedViewAt(0, releasedChild.getTop());
			 * invalidate();
			 */
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            postDelayed(mPeekRunnable, PEEK_DELAY);
        }

        private void peekDrawer() {
            final View toCapture = findContentView();
            final int childLeft;
            final int peekDistance = mDragger.getEdgeSize();
            childLeft = getWidth() - peekDistance;
            // Only peek if it would mean making the drawer more visible and the
            // drawer isn't locked
            if (toCapture != null) {
                final LayoutParams lp = (LayoutParams) toCapture.getLayoutParams();
                mDragger.smoothSlideViewTo(toCapture, childLeft, toCapture.getTop());
                //

                lp.isPeeking = true;
                invalidate();

                cancelChildViewTouch();
            }
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            if (ALLOW_EDGE_LOCK) {
                return true;
            }
            return true;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            final View toCapture = findContentView();

            if (toCapture != null) {
                mDragger.captureChildView(toCapture, pointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int width = getWidth();

            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }
    }

}
