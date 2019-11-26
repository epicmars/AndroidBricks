package com.androidpi.app.bricks.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Random;
/**
 *
 * Created by jastrelax on 2015/9/3.
 */
public class SnakeGameView extends View {

    private static final int MAX_LENGTH = 10;
    private static final int MAX_INX = MAX_LENGTH - 1;
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;
    private int[][] mMap;
    private int mBlockWidth;
    //
    private LinkedList<Block> mSnake;
    private Block mFood;
    private int mNext = LEFT;
    private boolean isGameOver = false;
    private boolean isFoodEaten = false;
    private Random mRandom;

    //
    private Paint mLinePaint;
    private Paint mSnakePaint;
    private Paint mFoodPaint;
    private Paint mBlockPaint;

    //
    private GestureDetectorCompat mGestureDectector;
    private OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent e) {
            return true;
        }

        ;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(SnakeGameView.class.getName(), String.format("distanceX: %f distanceY: %f", distanceX, distanceY));
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(SnakeGameView.class.getName(), String.format("velocityX: %f velocityY: %f", velocityX, velocityY));
            if (e2.getY() < e1.getY() && velocityY < 0 && Math.abs(velocityX) < Math.abs(velocityY)) {
                if (mNext != DOWN) {
                    mNext = UP;
                }
            } else if (e2.getY() > e1.getY() && velocityY > 0 && Math.abs(velocityX) < Math.abs(velocityY)) {
                if (mNext != UP) {
                    mNext = DOWN;
                }
            } else if (e2.getX() < e1.getX() && velocityX < 0 && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (mNext != RIGHT) {
                    mNext = LEFT;
                }
            } else if (e2.getX() > e1.getX() && velocityX > 0 && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (mNext != LEFT) {
                    mNext = RIGHT;
                }
            }
            return true;
        }
    };

    public SnakeGameView(Context context) {
        this(context, null);
    }

    public SnakeGameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnakeGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGame(context);
        initPaint(context);
        initGestureDectector(context);
    }

    private void initGame(Context context) {
        mMap = new int[MAX_LENGTH][MAX_LENGTH];
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mBlockWidth = width / MAX_LENGTH;

        mSnake = new LinkedList<>();
        int middleInx = MAX_LENGTH / 2;
        mSnake.add(new Block(middleInx - 1, middleInx));
        mSnake.add(new Block(middleInx, middleInx));
        mSnake.add(new Block(middleInx + 1, middleInx));
        //
        mRandom = new Random();
        mFood = new Block();
        mFood.x = mRandom.nextInt(MAX_LENGTH);
        mFood.y = mRandom.nextInt(MAX_LENGTH);
    }

    private void initGestureDectector(Context context) {
        mGestureDectector = new GestureDetectorCompat(context, mGestureListener);
    }

    private void initPaint(Context context) {
        mLinePaint = new Paint();
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(1f);

        mSnakePaint = new Paint();
        mSnakePaint.setStyle(Style.FILL);
        mSnakePaint.setColor(Color.BLACK);

        mFoodPaint = new Paint();
        mFoodPaint.setStyle(Style.FILL);
        mFoodPaint.setColor(Color.CYAN);

        mBlockPaint = new Paint();
        mBlockPaint.setStyle(Style.FILL_AND_STROKE);
        mBlockPaint.setColor(Color.WHITE);
        mBlockPaint.setStrokeWidth(1f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paintMap(canvas);
        paintFood(canvas);

        refreshSnake();
        paintSnake(canvas);
        if (!isGameOver) {
            postInvalidateDelayed(500);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDectector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    //
    private void paintMap(Canvas canvas) {
        for (int i = 0; i < MAX_LENGTH + 1; i++) {
            canvas.drawLine(0, i * mBlockWidth, mBlockWidth * MAX_LENGTH, i * mBlockWidth, mLinePaint);
            canvas.drawLine(i * mBlockWidth, 0, i * mBlockWidth, mBlockWidth * MAX_LENGTH, mLinePaint);
        }
    }

    private void refreshSnake() {
        Block head = mSnake.get(0);
        Block nextHead = new Block();
        switch (mNext) {
            case UP:
                nextHead.x = head.x;
                nextHead.y = head.y - 1;
                break;
            case DOWN:
                nextHead.x = head.x;
                nextHead.y = head.y + 1;
                break;
            case LEFT:
                nextHead.x = head.x - 1;
                nextHead.y = head.y;
                break;
            case RIGHT:
                nextHead.x = head.x + 1;
                nextHead.y = head.y;
                break;
        }
        if (!isGameOver(nextHead)) {
            if (!isFood(nextHead)) {
                mSnake.removeLast();
            } else {
                isFoodEaten = true;
            }
            mSnake.addFirst(nextHead);
        } else {
            isGameOver = true;
            Toast.makeText(getContext(), "Game over!", Toast.LENGTH_LONG).show();
        }
    }

    private void paintSnake(Canvas canvas) {
        for (Block block : mSnake) {
            paintBlock(block, canvas, mSnakePaint);
        }
    }

    private boolean isFood(Block nextHead) {
        return nextHead.equals(mFood);
    }

    private boolean isGameOver(Block nextHead) {
        if (nextHead.x < 0 || nextHead.x > 9 || nextHead.y < 0 || nextHead.y > 9) {
            return true;
        }
        for (Block block : mSnake) {
            if (nextHead.x == block.x && nextHead.y == block.y) {
                return true;
            }
        }
        return false;
    }

    private void paintFood(Canvas canvas) {
        if (isFoodEaten) {
            mFood.x = mRandom.nextInt(MAX_LENGTH);
            mFood.y = mRandom.nextInt(MAX_LENGTH);
            isFoodEaten = false;
        }
        paintBlock(mFood, canvas, mFoodPaint);
    }

    private void paintBlock(Block block, Canvas canvas, Paint paint) {
        int left = block.x * mBlockWidth;
        int top = block.y * mBlockWidth;
        canvas.drawRect(new Rect(left, top, left + mBlockWidth, top + mBlockWidth), paint);
    }

    private static class Block {
        int x;
        int y;

        public Block() {

        }

        public Block(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Block(Block block) {
            this.x = block.x;
            this.x = block.y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Block other = (Block) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }

    }

}
