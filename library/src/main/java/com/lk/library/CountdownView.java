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
 * creator: lk
 * time: 2017/4/18
 * Description: CountdownView.
 */

public class CountdownView extends View {

    private static final String TAG = "CountdownView";

    private final int DEFAULT_PROGRESS_COLOR       = 0xFFEEEEEE;
    private final int DEFAULT_PROGRESS_LIGHT_COLOR = 0xFFFF0000;
    private final int DEFAULT_TEXT_COLOR           = 0xFF212121;
    private final int DEFAULT_BG_COLOR             = 0xFFFFFFFF;

    private Paint mBgPaint;
    private int mBgColor = DEFAULT_BG_COLOR;

    private Paint mProgressPaint;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;

    private Paint mProgressLightPaint;
    private int mProgressLightColor = DEFAULT_PROGRESS_LIGHT_COLOR;

    private Paint mTextPaint;
    private int    mTextColor  = DEFAULT_TEXT_COLOR;
    private float  mTextSize   = 12;
    private String mCenterText = "";

    private int mProgressWidth;
    //current progress 0-100
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
        //Paint config for default progress.默认进度画笔设置
        mProgressPaint = new Paint();
        mProgressPaint.setStrokeWidth(mProgressWidth = dip2px(getContext(), 3));
        mProgressPaint.setStyle(Paint.Style.STROKE);
        //Paint config for light progress.倒计时转过的画笔设置
        mProgressLightPaint = new Paint();
        mProgressLightPaint.setStrokeWidth(mProgressWidth = dip2px(getContext(), 3));
        mProgressLightPaint.setStyle(Paint.Style.STROKE);
        //Paint config for text progress.文字画笔设置
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);

        mBgPaint = new Paint();
        mBgPaint.setStyle(Paint.Style.FILL);
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
        //Use the largest when width and height are not same
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

        mBgPaint.setColor(mBgColor);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - mProgressWidth, mBgPaint);

        //Show interval if center text is null
        if (mCenterText == null) {
            mIsShowInterval = true;
        }

        if (mIsShowInterval) {
            //Parse surplus time to seconds
            String surplusS = String.valueOf(mSurplusDuration / 1000 +
                    (mSurplusDuration != mDuration ? 1 : 0));
            //Finish if surplus time < 0
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
     * draw default progress
     *
     * @param canvas canvas
     */
    private void drawProgress(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - mProgressWidth,
                mProgressPaint);
    }

    /**
     * draw progress light
     *
     * @param canvas canvas
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
     * draw text
     *
     * @param canvas canvas
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
     * start countdown
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
     * set countdown duration
     *
     * @param duration duration. unit:millisecond
     */
    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    /**
     * set default progress color
     *
     * @param color color ex.0xFFFFFFFF
     */
    public void setProgressColor(@ColorInt int color) {
        this.mProgressColor = color;
    }

    /**
     * set progress light color
     *
     * @param color color ex.0xFFFFFFFF
     */
    public void setProgressLightColor(@ColorInt int color) {
        this.mProgressLightColor = color;
    }

    /**
     * set center text
     *
     * @param centerText the text you want show
     */
    public void setText(@NonNull String centerText) {
        this.mCenterText = centerText;
        mIsShowInterval = false;
    }

    /**
     * set center text color
     *
     * @param color color ex.0xFFFFFFFF
     */
    public void setTextColor(@ColorInt int color) {
        this.mTextColor = color;
    }

    /**
     * set center text size
     *
     * @param sp text size unit：sp
     */
    public void setTextSize(float sp) {
        this.mTextSize = sp2px(getContext(), sp);
    }

    public void setBgColor(@ColorInt int color) {
        this.mBgColor = color;
    }

    /**
     * set progress listener
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
