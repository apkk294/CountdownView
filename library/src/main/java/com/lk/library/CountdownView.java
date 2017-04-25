package com.lk.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * 创建者： lk
 * 时间：2017/4/18
 * Description：倒计时View.
 */

public class CountdownView extends View {

    private static final String TAG = "CountdownView";

    private final int DEFAULT_PROGRESS_COLOR       = 0xFFEEEEEE;
    private final int DEFAULT_PROGRESS_LIGHT_COLOR = 0xFFFF0000;
    private final int DEFAULT_TEXT_COLOR           = 0xFF212121;

    private Paint mProgressPaint;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;

    private Paint mProgressLightPaint;
    private int mProgressLightColor = DEFAULT_PROGRESS_LIGHT_COLOR;

    private Paint mTextPaint;
    private int    mTextColor  = DEFAULT_TEXT_COLOR;
    private float  mTextSize   = 12;
    private String mCenterText = "";

    private int mProgressWidth;
    //当前进度0-100
    private int mCurProgress;
    private long mDuration        = 3000;
    private long mSurplusDuration = mDuration;

    private boolean mIsShowInterval = true;

    private CountdownListener mCountdownListener;

    public CountdownView(Context context) {
        super(context);
        initPaint();
    }

    public CountdownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        obtainAttr(attrs);
        initPaint();
    }

    public CountdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttr(attrs);
        initPaint();
    }

    private void obtainAttr(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CountdownView);

        mDuration = (long) (typedArray.getFloat(R.styleable.CountdownView_duration, 3) * 1000);
        mProgressColor = typedArray.getColor(R.styleable.CountdownView_progress_color, DEFAULT_PROGRESS_COLOR);
        mProgressLightColor = typedArray.getColor(R.styleable.CountdownView_progress_light_color, DEFAULT_PROGRESS_LIGHT_COLOR);
        if (typedArray.hasValue(R.styleable.CountdownView_center_text)) {
            mCenterText = typedArray.getString(R.styleable.CountdownView_center_text);
            mIsShowInterval = false;
        }
        mTextColor = typedArray.getColor(R.styleable.CountdownView_center_text_color, DEFAULT_TEXT_COLOR);
        mTextSize = typedArray.getDimension(R.styleable.CountdownView_center_text_size, sp2px(getContext(), 12));

        typedArray.recycle();
    }

    private void initPaint() {
        //默认进度条画笔设置
        mProgressPaint = new Paint();
        mProgressPaint.setStrokeWidth(mProgressWidth = dip2px(getContext(), 3));
        mProgressPaint.setStyle(Paint.Style.STROKE);
        //高亮时进度条画笔设置
        mProgressLightPaint = new Paint();
        mProgressLightPaint.setStrokeWidth(mProgressWidth = dip2px(getContext(), 3));
        mProgressLightPaint.setStyle(Paint.Style.STROKE);
        //文字画笔
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(widthMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            width = dip2px(getContext(), 50);
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height = dip2px(getContext(), 50);
        }
        //宽高不相同的时候哪个大按哪个
        if (width != height) {
            width = height = Math.max(width, height);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mProgressPaint.setColor(mProgressColor);
        mProgressLightPaint.setColor(mProgressLightColor);

        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        //如果文字是null的话，显示倒计时
        if (mCenterText == null) {
            mIsShowInterval = true;
        }

        if (mIsShowInterval) {
            //剩余时间转换成秒
            String surplusS = String.valueOf(mSurplusDuration / 1000 +
                    (mSurplusDuration != mDuration ? 1 : 0));
            //剩余时间小于等于0说明已经结束
            if (mSurplusDuration <= 0) {
                surplusS = "0";
            }
            mCenterText = surplusS + "s";
        }

        drawProgress(canvas);
        drawText(canvas);
        drawProgressLight(canvas);
    }

    /**
     * 画圆圈
     *
     * @param canvas 画布
     */
    private void drawProgress(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - mProgressWidth,
                mProgressPaint);
    }

    /**
     * 画高亮的圆圈
     *
     * @param canvas 画布
     */
    private void drawProgressLight(Canvas canvas) {
        canvas.save();
        canvas.rotate(-90, getWidth() / 2, getHeight() / 2);

        RectF progressLightRect = new RectF(mProgressWidth, mProgressWidth,
                getWidth() - mProgressWidth, getHeight() - mProgressWidth);

        float sweep = obtainPercent() * 360;

        canvas.drawArc(progressLightRect, 0, sweep, false, mProgressLightPaint);

        canvas.restore();
    }

    /**
     * 画文字
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas) {
        Rect textBound = new Rect();
        mTextPaint.getTextBounds(mCenterText, 0, mCenterText.length(), textBound);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        canvas.drawText(mCenterText, getWidth() / 2 - textBound.width() / 2,
                getHeight() / 2 - fontMetrics.descent + (fontMetrics.descent - fontMetrics.ascent) / 2,
                mTextPaint);
    }

    private float obtainPercent() {
        return (float) mCurProgress / 100;
    }

    /**
     * 开始倒计时
     */
    public void start() {
        startAnim();
    }

    private void startAnim() {
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.setDuration(mDuration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurProgress = (int) animation.getAnimatedValue();
                mSurplusDuration = mDuration - animation.getCurrentPlayTime();
                if (mCountdownListener != null) {
                    mCountdownListener.onProgressListener(mCurProgress, mCurProgress == 100);
                }
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 设置倒计时时长
     *
     * @param duration 时长 毫秒
     */
    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    /**
     * 设置圆圈的颜色
     *
     * @param color int型颜色0xFFFFFFFF
     */
    public void setProgressColor(@ColorInt int color) {
        this.mProgressColor = color;
    }

    /**
     * 设置高亮圆圈的颜色
     *
     * @param color int型颜色0xFFFFFFFF
     */
    public void setProgressLightColor(@ColorInt int color) {
        this.mProgressLightColor = color;
    }

    /**
     * 设置中间文字
     *
     * @param centerText 中间要显示的文字
     */
    public void setText(@NonNull String centerText) {
        this.mCenterText = centerText;
        mIsShowInterval = false;
    }

    /**
     * 设置中间文字的颜色
     *
     * @param color int型颜色0xFFFFFFFF
     */
    public void setTextColor(@ColorInt int color) {
        this.mTextColor = color;
    }

    /**
     * 设置中间文字的大小
     *
     * @param sp 文字大小 单位：sp
     */
    public void setTextSize(float sp) {
        this.mTextSize = sp2px(getContext(), sp);
    }

    /**
     * 设置进度监听
     *
     * @param countdownListener {@link CountdownListener}
     */
    public void setCountdownListener(CountdownListener countdownListener) {
        this.mCountdownListener = countdownListener;
    }

    public interface CountdownListener {
        void onProgressListener(int progress, boolean isFinish);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
