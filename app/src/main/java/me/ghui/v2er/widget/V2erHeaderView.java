package me.ghui.v2er.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.orhanobut.logger.Logger;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;
import me.ghui.v2er.R;
import me.ghui.v2er.util.ScaleUtils;

/**
 * Created by ghui on 24/09/2017.
 */

public class V2erHeaderView extends View implements PtrUIHandler, ValueAnimator.AnimatorUpdateListener {
    private Path mLeftPath = new Path();
    private Path mRightPath = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int MAIN_COLOR = Color.BLACK;
    //the short length of the glyph arrow
    private final int mDelta = ScaleUtils.dp(8);
    private float mScrollRatio = 0f;
    private float mRotation = 0.0f;
    private ValueAnimator mRotationAnimator;
    private int bgColor;
    private Paint clearPaint = new Paint();

    public V2erHeaderView(Context context) {
        super(context);
        init();
    }

    public V2erHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public V2erHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public V2erHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint.setColor(MAIN_COLOR);
        mPaint.setStyle(Paint.Style.FILL);

        mDividerPaint.setStyle(Paint.Style.STROKE);
        int strokeWidth = ScaleUtils.dp(0.5f);
        mDividerPaint.setStrokeWidth(strokeWidth);
        bgColor = getResources().getColor(R.color.divider_color);
        mDividerPaint.setColor(bgColor);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(8 * mDelta, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int cW = w - getPaddingStart() - getPaddingEnd();

        canvas.drawRect(0, 0, w, h, clearPaint);
        canvas.drawColor(Color.WHITE);
        canvas.drawLine(0, h - mDividerPaint.getStrokeWidth() / 2f, w, h - mDividerPaint.getStrokeWidth() / 2f, mDividerPaint);
        canvas.translate(cW / 2, h / 2);
        mLeftPath.reset();
        mRightPath.reset();
        // config path
        mLeftPath.moveTo(mDelta / 2, 0);
        mLeftPath.rLineTo(-mDelta, -mDelta);
        mLeftPath.rLineTo(-mDelta, 0);
        mLeftPath.rLineTo(mDelta, mDelta);
        mLeftPath.rLineTo(-mDelta, mDelta);
        mLeftPath.rLineTo(mDelta, 0);
        mLeftPath.close();
        //rightPath
        mRightPath.moveTo(-mDelta / 2, 0);
        mRightPath.rLineTo(mDelta, -mDelta);
        mRightPath.rLineTo(mDelta, 0);
        mRightPath.rLineTo(-mDelta, mDelta);
        mRightPath.rLineTo(mDelta, mDelta);
        mRightPath.rLineTo(-mDelta, 0);
        mRightPath.close();
        //calculate offset
        float totalDistance = cW / 2f - 1.5f * mDelta;
        float leftOffset = -mScrollRatio * totalDistance;
        float rightOffset = mScrollRatio * totalDistance;
        mLeftPath.offset(leftOffset, 0);
        mRightPath.offset(rightOffset, 0);

        canvas.save();
        canvas.rotate(mRotation * 360);
        canvas.drawPath(mLeftPath, mPaint);
        canvas.drawPath(mRightPath, mPaint);
        canvas.restore();
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        Logger.d("onUIRefreshBegin");
        if (mRotationAnimator != null && mRotationAnimator.isRunning()) return;
        mRotationAnimator = ValueAnimator.ofFloat(0f, 1f);
        mRotationAnimator.setInterpolator(new LinearInterpolator());
        mRotationAnimator.addUpdateListener(this);
        mRotationAnimator.setDuration(800);
        mRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotationAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRotationAnimator.start();
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        float offsetToRefresh = frame.getOffsetToRefresh();
        float offsetWhenKeepHeader = frame.getOffsetToKeepHeaderWhileLoading();
        int currentPos = ptrIndicator.getCurrentPosY();
        mScrollRatio = Math.max(1 - currentPos / offsetToRefresh, 0);
        Logger.d("mOffsetToRefresh: " + offsetToRefresh + ", currentPos: " + currentPos + ", mScrollRatio: " + mScrollRatio);
        invalidate();
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {
        if (mRotationAnimator == null) return;
        mRotationAnimator.cancel();
        mRotationAnimator = null;
        mRotation = 0;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mRotation = (float) animation.getAnimatedValue();
        invalidate();
    }
}