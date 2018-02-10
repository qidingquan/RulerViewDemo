package com.example.qdq.rulerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


public class RulerView extends View {

    private int mMinVelocity;
    private Scroller mScroller;  //Scroller是一个专门用于处理滚动效果的工具类   用mScroller记录/计算View滚动的位置，再重写View的computeScroll()，完成实际的滚动
    private VelocityTracker mVelocityTracker; //主要用跟踪触摸屏事件（flinging事件和其他gestures手势事件）的速率。
    private int mWidth;
    private int mHeight;

    private float mSelectorValue = 50.0f; // 未选择时 默认的值 滑动后表示当前中间指针正在指着的值
    private float mMaxValue = 200;        // 最大数值
    private float mMinValue = 100.0f;     //最小的数值
    private float mPerValue = 1;          //最小单位  如 1:表示 每2条刻度差为1.   0.1:表示 每2条刻度差为0.1
    private int mKeyValue = 5;               //每隔几个刻度绘制文字
    // 在demo中 身高mPerValue为1  体重mPerValue 为0.1

    private float mLineSpaceWidth = 50;    //  尺子刻度2条线之间的距离
    private float mLineMaxHeight = 15;   //  尺子刻度分为3中不同的高度。 mLineMaxHeight表示最长的那根(也就是 10的倍数时的高度)
    private float mLineMidHeight = 10;    //  mLineMidHeight  表示中间的高度(也就是 5  15 25 等时的高度)
    private float mLineMinHeight = 10;    //  mLineMinHeight  表示最短的那个高度(也就是 1 2 3 4 等时的高度)
    private float mLineHeight = 2;          //中间线的高度
    private int mLineMaxColor = Color.BLACK;//最大刻度颜色
    private int mLineMidColor = Color.GRAY; //中间刻度颜色
    private int mLineMinColor = Color.LTGRAY;//最小刻度颜色

    private int mLineColor = Color.LTGRAY;   //中间线的颜色
    private int mTextColor = Color.BLACK;    //文字的颜色

    private float mTextMarginTop = 10;    //o
    private float mTextSize = 30;         //尺子刻度下方数字 textsize

    private boolean mAlphaEnable = false;  // 尺子 最左边 最后边是否需要透明 (透明效果更好点)

    private float mTextHeight;            //尺子刻度下方数字  的高度

    private Paint mTextPaint;             // 尺子刻度下方数字( 也就是每隔10个出现的数值) paint
    private Paint mLinePaint;             //  尺子刻度  paint

    private int mTotalLine;               //共有多少条 刻度
    private int mMaxOffset;               //所有刻度 共有多长
    private float mOffset;                // 默认状态下，mSelectorValue所在的位置  位于尺子总刻度的位置
    private int mLastX, mMove;
    private OnValueChangeListener mListener;  // 滑动后数值回调

    public RulerView(Context context) {
        this(context, null);

    }

    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mScroller = new Scroller(context);


        this.mLineSpaceWidth = myfloat(mLineSpaceWidth);
        this.mLineMaxHeight = myfloat(mLineMaxHeight);
        this.mLineMidHeight = myfloat(mLineMidHeight);
        this.mLineMinHeight = myfloat(mLineMinHeight);
        this.mTextHeight = myfloat(mTextHeight);
        this.mLineHeight = myfloat(mLineHeight);


        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RulerView);

        mAlphaEnable = typedArray.getBoolean(R.styleable.RulerView_alphaEnable, mAlphaEnable);

        mLineSpaceWidth = typedArray.getDimension(R.styleable.RulerView_lineSpaceWidth, mLineSpaceWidth);
        mLineMaxHeight = typedArray.getDimension(R.styleable.RulerView_lineMaxHeight, mLineMaxHeight);
        mLineMidHeight = typedArray.getDimension(R.styleable.RulerView_lineMidHeight, mLineMidHeight);
        mLineMinHeight = typedArray.getDimension(R.styleable.RulerView_lineMinHeight, mLineMinHeight);
        mLineMaxColor = typedArray.getColor(R.styleable.RulerView_lineMaxColor, mLineMaxColor);
        mLineMidColor = typedArray.getColor(R.styleable.RulerView_lineMidColor, mLineMidColor);
        mLineMinColor = typedArray.getColor(R.styleable.RulerView_lineMinColor, mLineMinColor);
        mLineColor = typedArray.getColor(R.styleable.RulerView_lineColor, mLineColor);
        mLineHeight = typedArray.getDimension(R.styleable.RulerView_lineHeight, mLineHeight);

        mTextSize = typedArray.getDimension(R.styleable.RulerView_textSize, mTextSize);
        mTextColor = typedArray.getColor(R.styleable.RulerView_textColor, mTextColor);
        mTextMarginTop = typedArray.getDimension(R.styleable.RulerView_textMarginTop, mTextMarginTop);

        mSelectorValue = typedArray.getFloat(R.styleable.RulerView_selectorValue, mSelectorValue);
        mMinValue = typedArray.getFloat(R.styleable.RulerView_minValue, mMinValue);
        mMaxValue = typedArray.getFloat(R.styleable.RulerView_maxValue, mMaxValue);
        mPerValue = typedArray.getFloat(R.styleable.RulerView_perValue, mPerValue);
        mKeyValue = typedArray.getInteger(R.styleable.RulerView_keyValue, mKeyValue);

        typedArray.recycle();

        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextHeight = getFontHeight(mTextPaint);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mLineColor);

        setValue(mSelectorValue,mMinValue,mMaxValue,mPerValue);

    }


    public static int myfloat(float paramFloat) {
        return (int) (0.5F + paramFloat * 1.0f);
    }

    private float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }


    /**
     * @param selectorValue 未选择时 默认的值 滑动后表示当前中间指针正在指着的值
     * @param minValue      最大数值
     * @param maxValue      最小的数值
     * @param per           最小单位  如 1:表示 每2条刻度差为1.   0.1:表示 每2条刻度差为0.1 在demo中 身高mPerValue为1  体重mPerValue 为0.1
     */
    public void setValue(float selectorValue, float minValue, float maxValue, float per) {
        this.mSelectorValue = selectorValue;
        this.mMaxValue = maxValue;
        this.mMinValue = minValue;
        this.mPerValue = per;
        this.mTotalLine = ((int) ((mMaxValue - mMinValue) / mPerValue)) + 1;


        mMaxOffset = (int) (-(mTotalLine - 1) * mLineSpaceWidth);
        mOffset = (mMinValue - mSelectorValue) / mPerValue * mLineSpaceWidth;

        notifyValueChange();
        invalidate();
        setVisibility(VISIBLE);
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

        if(heightMode==MeasureSpec.AT_MOST){
            heightSize= (int) (mLineMaxHeight + mTextMarginTop + mTextHeight);
        }
        setMeasuredDimension(widthMeasureSpec,heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mWidth = w;
            mHeight = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left, radius;
        String value;
        int alpha = 0;
        float scale;
        int srcPointX = mWidth / 2;
        //绘制底线
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineHeight);
        canvas.drawLine(srcPointX + mOffset, mLineMaxHeight / 2, srcPointX + mOffset + (mTotalLine - 1) * mLineSpaceWidth, mLineMaxHeight / 2, mLinePaint);

        for (int i = 0; i < mTotalLine; i++) {
            left = srcPointX + mOffset + i * mLineSpaceWidth;

            if (left < 0 || left > mWidth) {
                continue;  //  先画默认值在正中间，左右各一半的view。  多余部分暂时不画(也就是从默认值在中间，画旁边左右的刻度线)
            }

            if (i % mKeyValue == 0) {
                radius = mLineMidHeight / 2;
                mLinePaint.setColor(mLineMidColor);
            } else {
                radius = mLineMinHeight / 2;
                mLinePaint.setColor(mLineMinColor);
            }
            if (mAlphaEnable) {
                scale = 1 - Math.abs(left - srcPointX) / srcPointX;
                alpha = (int) (255 * scale * scale);

                mLinePaint.setAlpha(alpha);
            }
            mLinePaint.setStrokeWidth(0);
            canvas.drawCircle(left, mLineMaxHeight / 2, radius, mLinePaint);

            if (i % mKeyValue == 0) {
                value = String.valueOf(mMinValue + i * mPerValue);
                if (mAlphaEnable) {
                    mTextPaint.setAlpha(alpha);
                }
                canvas.drawText(value, left - mTextPaint.measureText(value) / 2,
                        mLineMaxHeight / 2 + mTextMarginTop + mTextHeight, mTextPaint);    // 在为整数时,画 数值
            }

        }
        //绘制中间选择位置
        mLinePaint.setColor(mLineMaxColor);
        canvas.drawCircle(srcPointX, mLineMaxHeight / 2, mLineMaxHeight / 2, mLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int xPosition = (int) event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker();
                return false;
            default:
                break;
        }

        mLastX = xPosition;
        return true;
    }

    private void countVelocityTracker() {

        mVelocityTracker.computeCurrentVelocity(1000);  //初始化速率的单位
        float xVelocity = mVelocityTracker.getXVelocity(); //当前的速度
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }


    /**
     * 滑动结束后，若是指针在2条刻度之间时，改变mOffset 让指针正好在刻度上。
     */
    private void countMoveEnd() {

        mOffset -= mMove;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mLastX = 0;
        mMove = 0;

        mSelectorValue = mMinValue + Math.round(Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue;
        mOffset = (mMinValue - mSelectorValue) / mPerValue * mLineSpaceWidth;


        notifyValueChange();
        postInvalidate();
    }


    /**
     * 滑动后的操作
     */
    private void changeMoveAndValue() {
        mOffset -= mMove;

        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
            mMove = 0;
            mScroller.forceFinished(true);
        } else if (mOffset >= 0) {
            mOffset = 0;
            mMove = 0;
            mScroller.forceFinished(true);
        }
        mSelectorValue = mMinValue + Math.round(Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue;


        notifyValueChange();
        postInvalidate();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            mListener.onValueChange(mSelectorValue);
        }
    }


    /**
     * 滑动后的回调
     */
    public interface OnValueChangeListener {
        void onValueChange(float value);
    }

    @Override
    public void computeScroll() {

        super.computeScroll();
        if (mScroller.computeScrollOffset()) {     //mScroller.computeScrollOffset()返回 true表示滑动还没有结束
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }
}
