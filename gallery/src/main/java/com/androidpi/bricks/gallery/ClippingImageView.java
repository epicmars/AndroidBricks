package com.androidpi.bricks.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import timber.log.Timber;

public class ClippingImageView extends AppCompatImageView {

    private static final float END_ZOOMER = 0.25f;

    private Context mContext;

    // 视图相关域
    private float mClipLengthRatio;
    private float mClipLength;
    private RectF mContentRect; // 内容区
    private RectF mClippingRect; // 图形裁剪区
    private RectF mDrawRectF; // 图形绘制区
    private RectF mCurrentViewport; // 裁剪区在绘制区中的相对位置

    //
    private BitmapDrawable mDrawable;
    private Matrix mMatrix;
    private float[] mMatValues = new float[9];
    private float mLastScaleFactor = 1.0f;
    private float mMinScaleFactor = 1.0f;
    //
    private Paint mMaskPaint; // 遮罩画笔
    private Paint mClipPaint; // 裁剪区画笔

    private boolean mImgClipped = false; // 是否进行过裁剪

    // 触控操作
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private Zoomer mZoomer;
    private PointF mZoomFocalPoint = new PointF();

    private OverScroller mScroller;
    private int mLastScrollX;
    private int mLastScrollY;

    //
    private PointF mScrollMin = new PointF();
    private PointF mScrollMax = new PointF();
    private PointF mScrollOver = new PointF();
    private PointF mFocalPoint = new PointF();
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // lastSpanX = ScaleGestureDetectorCompat.getCurrentSpanX(detector);
            // lastSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(detector);
            // mScaleFactor = mLastScaleFactor;
            mZoomer.forceFinished(true);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // float spanX =
            // ScaleGestureDetectorCompat.getCurrentSpanX(detector);
            // float spanY =
            // ScaleGestureDetectorCompat.getCurrentSpanY(detector);
            // double scaleX = spanX / lastSpanX;
            // double scaleY = spanY / lastSpanY;
            // float scale = (float) Math.min(scaleX, scaleY);// TODO
            // 放大基准，X轴向放大，Y轴向缩小，则会导致放大
            float relativeScale = detector.getCurrentSpan() / detector.getPreviousSpan();
            float scaleFactor = relativeScale * mLastScaleFactor;

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            hitTest(focusX, focusY, mFocalPoint);

            scaleDrawable(mFocalPoint, relativeScale, scaleFactor);

            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            // 限制缩放比例,短边匹配
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            hitTest(focusX, focusY, mFocalPoint);
        }

        ;
    };
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent e) {
            return true;
        }

        ;

        public boolean onSingleTapConfirmed(MotionEvent e) {
            performClick();
            return true;
        }

        ;

        public boolean onDoubleTap(MotionEvent e) {
            mZoomer.forceFinished(true);
            float currentZoom = mZoomer.getCurrZoom();
            if (currentZoom <= END_ZOOMER) {
                mLastScaleFactor += currentZoom; // 记录当前放大系数
            }
            //
            float x = e.getX();
            float y = e.getY();
            hitTest(x, y, mZoomFocalPoint);

            mZoomer.startZoom(END_ZOOMER);
            ViewCompat.postInvalidateOnAnimation(ClippingImageView.this);
            return true;
        }

        ;

        public boolean onDoubleTapEvent(MotionEvent e) {

            int action = e.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    springBackToClippingRect();
                    break;
            }
            return true;
        }

        ;

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mMatrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(mMatrix);
            scroll(0, 0);
            return true;
        }

        ;

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling((int) velocityX, (int) velocityY);
            return true;
        }

        ;

    };

    // public ClippingImageView(Context context, AttributeSet attrs,
    // int defStyleAttr, int defStyleRes) {
    // super(context, attrs, defStyleAttr, defStyleRes);
    // }

    public ClippingImageView(Context context) {
        this(context, null, 0);
    }

    public ClippingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClippingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        // 获取视图属性
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClippingImageView, defStyleAttr, 0);
        try {
            mClipLengthRatio = a.getFraction(R.styleable.ClippingImageView_clipAreaRatio, 1, 1, 1.0f);
        } finally {
            if (a != null)
                a.recycle();
        }
        initFields();
        // initCanvas(); // 导致崩溃？
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mScroller = new OverScroller(context);
        mZoomer = new Zoomer(context);

    }

    private void initFields() {
        // TODO Auto-generated method stub
        mContentRect = new RectF();
        mClippingRect = new RectF();
        mDrawRectF = new RectF();
        mCurrentViewport = new RectF();
        mMatrix = new Matrix();

        mMaskPaint = new Paint();
        final int transparentBlack = 0x77000000;
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(transparentBlack);

        mClipPaint = new Paint();
        mClipPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mClipPaint.setColor(Color.BLACK);
        mClipPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        //
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 设置绘制区域
        mContentRect.set(0, 0, w, h);

        int maxClipLength = Math.min(w, h);
        mClipLength = (float) maxClipLength * mClipLengthRatio;
        float clipLeft = (w - mClipLength) / 2;
        float clipTop = (h - mClipLength) / 2;
        mClippingRect.set(clipLeft, clipTop, clipLeft + mClipLength, clipTop + mClipLength);
        initDrawable();
        //
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

        mDrawable = (BitmapDrawable) getDrawable();
        drawMask(canvas); // 对填充（padding）的处理
    }

    /**
     * 绘制遮罩
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        int sc = canvas.saveLayer(0, 0, mContentRect.right, mContentRect.bottom, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawRect(mContentRect, mMaskPaint);
        canvas.drawRect(mClippingRect, mClipPaint);
        canvas.restoreToCount(sc);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Timber.d("ACTION_UP");
                if (mLastScaleFactor < mMinScaleFactor) {
                    scaleDrawable(mFocalPoint, mMinScaleFactor / mLastScaleFactor, mMinScaleFactor);
                }
                springBackToClippingRect();
                break;
        }
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initDrawable();
    }

    /**
     * 初始化图像显示
     */
    private void initDrawable() {
        // 设置初始图像显示大小，使其短边等于裁剪区长度并居中显示
        final int cw = getWidth();
        final int ch = getHeight();
        mDrawable = (BitmapDrawable) getDrawable();
        if (mDrawable != null && mDrawable.getBitmap() != null) {
            int dw = mDrawable.getIntrinsicWidth();
            int dh = mDrawable.getIntrinsicHeight();
            float sf = 1.0f;
            float left = 0f, top = 0f;
            if (dw > 0 && dh > 0) {
                if (dw > dh) { // 图像宽度大于高度
                    float sw = dw * mClipLength / dh;
                    left = (cw - sw) / 2;
                    top = (ch - mClipLength) / 2;
                    sf = mClipLength / dh;
                } else {
                    float sh = dh * mClipLength / dw;
                    left = (cw - mClipLength) / 2;
                    top = (ch - sh) / 2;
                    sf = mClipLength / dw;
                }
                mLastScaleFactor = sf;
                mMinScaleFactor = sf;
                mMatrix.setScale(sf, sf);
                mMatrix.postTranslate(left, top);
                setImageMatrix(mMatrix);
            }
        }
    }

    private void scroll(float distanceX, float distanceY) {
        computeScrollSurfaceSize(mScrollOver, mScrollMin, mScrollMax);

        mScroller.forceFinished(true);
        RectF rect = getDrawRectF();
        if (rect != null) {
            int startX = (int) rect.left;
            int startY = (int) rect.top;

            mScroller.startScroll(startX, startY, (int) distanceX, (int) distanceY);
            mLastScrollX = startX;
            mLastScrollY = startY;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void fling(int velocityX, int velocityY) {
        computeScrollSurfaceSize(mScrollOver, mScrollMin, mScrollMax);

        mScroller.forceFinished(true);
        RectF rect = getDrawRectF();
        if (rect != null) {
            int startX = (int) rect.left;
            int startY = (int) rect.top;
            mScroller.fling(startX, startY, velocityX, velocityY, (int) mScrollMin.x, (int) mScrollMax.x,
                    (int) mScrollMin.y, (int) mScrollMax.y, (int) mScrollOver.x, (int) mScrollOver.y);

            mLastScrollX = startX;
            mLastScrollY = startY;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 计算滑动区域
     *
     * @param over 越界值，可以反向越过最小值，和正向越过最大值的范围
     * @param min  最小值
     * @param max  最大值
     */
    private void computeScrollSurfaceSize(PointF over, PointF min, PointF max) {
        RectF rect = getDrawRectF();
        if (rect != null) {
            final float dWidth = rect.width(), dHeight = rect.height();
            final float cWidth = mClippingRect.width(), cHeight = mClippingRect.height();

            if (dWidth > cWidth) {
                min.x = mClippingRect.left - (dWidth - cWidth);
                max.x = mClippingRect.left;
                over.x = cWidth;
            } else {
                min.x = mClippingRect.left;
                max.x = mClippingRect.left + (cWidth - dWidth);
                over.x = dWidth;
            }

            if (dHeight > cHeight) {
                min.y = mClippingRect.top - (dHeight - cHeight);
                max.y = mClippingRect.top;
                over.y = cHeight;
            } else {
                min.y = mClippingRect.top;
                max.y = mClippingRect.top + (cHeight - dHeight);
                over.x = dHeight;
            }
        }
    }

    /**
     * 点击测试并获取触控焦点
     *
     * @param x
     * @param y
     * @param focalPoint
     */
    private void hitTest(float x, float y, PointF focalPoint) {
        RectF rect = getDrawRectF();
        if (rect != null) {
            // 焦点在图像内
            if (rect.contains(x, y)) {
                focalPoint.set(x, y);
            } else {
                float centerX = rect.left + rect.width() / 2;
                // 上方
                if (y < rect.top) {
                    // 左边
                    if (x < centerX) {
                        focalPoint.set(rect.left, rect.top);
                    } else {
                        focalPoint.set(rect.right, rect.top);
                    }
                } else if (y > rect.bottom) { // 下方
                    if (x < centerX) {
                        focalPoint.set(rect.left, rect.bottom);
                    } else {
                        focalPoint.set(rect.right, rect.bottom);
                    }
                }
            }
        }
    }

    /**
     * 当先图像的绘制区域
     *
     * @return
     */
    private RectF getDrawRectF() {
        if (mDrawable != null && mDrawable.getBitmap() != null) {
            int dw = mDrawable.getIntrinsicWidth();
            int dh = mDrawable.getIntrinsicHeight();
            mMatrix.getValues(mMatValues);
            float left = mMatValues[2];
            float top = mMatValues[5];
            float right = left + mMatValues[0] * dw;
            float bottom = top + mMatValues[4] * dh;
            mDrawRectF.set(left, top, right, bottom);
            return mDrawRectF;
        }
        return null;
    }

    @SuppressLint("NewApi")
    @Override
    public void computeScroll() {
        super.computeScroll();

        boolean needInvalidate = false;
        boolean needCalibrate = false;
        if (mScroller.computeScrollOffset()) {
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();
            mMatrix.postTranslate(currX - mLastScrollX, currY - mLastScrollY);
            setImageMatrix(mMatrix);

            mLastScrollX = currX;
            mLastScrollY = currY;
            needInvalidate = true;
        } else {
            // if(mLastScaleFactor < mMinScaleFactor){
            // scaleDrawable(mFocalPoint, mMinScaleFactor / mLastScaleFactor,
            // mMinScaleFactor);
            // }
            // springBackToClippingRect();
            needCalibrate = true;
        }

        if (mZoomer.computeZoom()) {
            float currentZoom = mZoomer.getCurrZoom();
            float sf = mLastScaleFactor + currentZoom;
            float scale = sf / mLastScaleFactor;
            mLastScaleFactor = sf;
            // 进行缩放
            scaleDrawable(mZoomFocalPoint, scale, sf);
        }

        if (needCalibrate)
            calibrateDrawRectF();

        if (needInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    ;

    /**
     * 校准绘制区域
     */
    private void calibrateDrawRectF() {
        RectF rectf = getDrawRectF();
        if (rectf != null) {
            if (rectf.left > mClippingRect.left) {
                mMatrix.postTranslate(mClippingRect.left - rectf.left, 0);
            }
            if (rectf.top > mClippingRect.top) {
                mMatrix.postTranslate(0, mClippingRect.top - rectf.top);
            }
            if (rectf.right < mClippingRect.right) {
                mMatrix.postTranslate(mClippingRect.right - rectf.right, 0);
            }
            if (rectf.bottom < mClippingRect.bottom) {
                mMatrix.postTranslate(0, mClippingRect.bottom - rectf.bottom);
            }
            setImageMatrix(mMatrix);
        }
    }

    /**
     * 回弹到裁剪区
     */
    private void springBackToClippingRect() {
        if (mScroller.isFinished()) {
            computeScrollSurfaceSize(mScrollOver, mScrollMin, mScrollMax);
            RectF rect = getDrawRectF();
            if (rect != null) {
                // 判断图像是否在裁剪区内部
                if (!mClippingRect.contains(rect) && !rect.contains(mClippingRect)) {
                    mLastScrollX = (int) rect.left;
                    mLastScrollY = (int) rect.top;
                    springBack(rect.left, rect.top, mScrollMin.x, mScrollMax.x, mScrollMin.y, mScrollMax.y);
                }
            }
        }
    }

    /**
     * 回弹
     *
     * @param startX
     * @param startY
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    private void springBack(float startX, float startY, float minX, float maxX, float minY, float maxY) {
        mScroller.forceFinished(true);
        mScroller.springBack((int) startX, (int) startY, (int) minX, (int) maxX, (int) minY, (int) maxY);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 滑回
     *
     * @param startX
     * @param startY
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    private void scrollBack(float startX, float startY, float minX, float maxX, float minY, float maxY) {
        float dx = 0.0f, dy = 0.0f;
        if (startX > maxX) {
            dx = maxX - startX;
        } else if (startX < minX) {
            dx = minX - startX;
        }

        if (startY > maxY) {
            dy = maxY - startY;
        } else if (startY < minY) {
            dy = minY - startY;
        }
        mScroller.startScroll((int) startX, (int) startY, (int) dx, (int) dy);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 缩放绘图
     *
     * @param focalPoint
     * @param scale
     * @param scaleFactor
     */
    private void scaleDrawable(PointF focalPoint, float scale, float scaleFactor) {
        // 计算矩阵
        RectF rect = getDrawRectF();
        if (rect != null) {
            float newLeft = focalPoint.x - (focalPoint.x - rect.left) * scale;
            float newTop = focalPoint.y - (focalPoint.y - rect.top) * scale;
            mMatrix.setScale(scaleFactor, scaleFactor);
            mMatrix.postTranslate(newLeft, newTop);
        } else {
            mMatrix.setScale(scaleFactor, scaleFactor);
            mMatrix.getValues(mMatValues);
            mMatrix.postTranslate(mMatValues[2], mMatValues[5]);
        }
        setImageMatrix(mMatrix);
        mLastScaleFactor = scaleFactor;
    }

    /**
     * 获取当前视口
     *
     * @return
     */
    public RectF getCurrentViewport() {

        RectF rect = getDrawRectF();
        if (rect != null) {
            float leftOffset = mClippingRect.left - rect.left;
            float topOffset = mClippingRect.top - rect.top;
            float left = leftOffset / rect.width(); // 左点在原图中的相对位置
            float top = topOffset / rect.height();
            float right = (leftOffset + mClippingRect.width()) / rect.width(); // 右点在原图中相对位置
            float bottom = (topOffset + mClippingRect.height()) / rect.height();

            mCurrentViewport.set(left, top, right, bottom);
            return mCurrentViewport;
        }
        return null;
    }

    /**
     * 裁剪位图
     */
    public void clipImage() {
        RectF rect = getCurrentViewport();
        mDrawable = (BitmapDrawable) getDrawable();
        if (mDrawable != null && rect != null) {
            Bitmap src = mDrawable.getBitmap();
            Bitmap dst = null;
            int w = src.getWidth();
            int h = src.getHeight();
            int x = (int) (rect.left * w);
            int y = (int) (rect.top * h);
            int width = (int) (rect.width() * w);
            int height = (int) (rect.height() * h);
            try {
                if (src.isMutable()) {
                    dst = Bitmap.createBitmap(src, x, y, width, height);
                } else {
                    dst = src.copy(Config.ARGB_8888, true);
                    dst = Bitmap.createBitmap(dst, x, y, width, height);
                }
            } catch (IllegalArgumentException e) {

            }

            if (dst != null) {
                mDrawable = new BitmapDrawable(mContext.getResources(), dst);
                setImageDrawable(mDrawable);
                // 移动图像到裁剪区
                mMatrix.getValues(mMatValues);
                float left = mMatValues[2];
                float top = mMatValues[5];
                mMatrix.postTranslate(mClippingRect.left - left, mClippingRect.top - top);
                setImageMatrix(mMatrix);
                mImgClipped = true;
            }
        }
    }

    /**
     * 保存裁剪后的图像
     */
    public String saveClippedImage() {
        String savedPath = getSavedPath();
        if (mImgClipped) {
            mDrawable = (BitmapDrawable) getDrawable();
            if (mDrawable != null) {
                Bitmap bmp = mDrawable.getBitmap();

                OutputStream out = null;
                try {
                    out = new FileOutputStream(savedPath);
                    bmp.compress(CompressFormat.JPEG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mImgClipped = false;
        }
        return savedPath;
    }

    /**
     * 获取保存路径
     *
     * @return
     */
    private String getSavedPath() {
        // 获取保存路径并创建文件名
        final String currPath = (String) getTag();
        int lastSeperator = currPath.lastIndexOf(File.separatorChar);
        String currDir = currPath.substring(0, lastSeperator + 1);
        String currName = currPath.substring(lastSeperator + 1);
        //
        int lastDot = currName.lastIndexOf('.');
        String savedName = currName.substring(0, lastDot);

        int savedIndex = 1;
        StringBuilder savedPath = new StringBuilder();
        File savedFile = null;
        do {
            savedPath.delete(0, savedPath.length());
            savedPath.append(currDir).append(savedName).append('_').append(savedIndex).append('.').append("jpg");
            savedIndex++;
            savedFile = new File(savedPath.toString());
        } while (savedFile != null && savedFile.exists());

        return savedPath.toString();
    }

    /**
     * TODO 保存当前视口
     */

}
