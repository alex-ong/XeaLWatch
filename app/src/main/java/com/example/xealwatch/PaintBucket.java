package com.example.xealwatch;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PaintBucket {
    private BinaryPaint mHourPaint;
    private BinaryPaint mMinutePaint;
    private BinaryPaint mSecondPaint;
    private BinaryPaint mSmallTickPaint;
    private BinaryPaint mBigTickPaint;
    private static final float HOUR_STROKE_WIDTH = 15f;
    private static final float MINUTE_STROKE_WIDTH = 10f;
    private static final float SECOND_STROKE_WIDTH = 5f;
    private static final float LARGE_SECOND_TICK_STROKE_WIDTH = 14f;
    private static final float SMALL_SECOND_TICK_STROKE_WIDTH = 2f;

    public PaintBucket(int watchHandColor, int watchHandSecondColor, int watchTickColor) {
        mHourPaint = new BinaryPaint();
        mHourPaint.initializeActive(watchHandColor, HOUR_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mMinutePaint = new BinaryPaint();
        mMinutePaint.initializeActive(watchHandColor, MINUTE_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mSecondPaint = new BinaryPaint();
        mSecondPaint.initializeActive(watchHandSecondColor, SECOND_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mSmallTickPaint = new BinaryPaint();
        mSmallTickPaint.initializeActive(watchTickColor, SMALL_SECOND_TICK_STROKE_WIDTH, Paint.Cap.BUTT, Paint.Style.STROKE);

        mBigTickPaint = new BinaryPaint();
        mBigTickPaint.initializeActive(watchTickColor, LARGE_SECOND_TICK_STROKE_WIDTH, Paint.Cap.BUTT, Paint.Style.STROKE);
    }

    public BinaryPaint getHourPaint() {
        return mHourPaint;
    }

    public BinaryPaint getMinutePaint() {
        return mMinutePaint;
    }

    public BinaryPaint getSecondPaint() {
        return mSecondPaint;
    }

    public BinaryPaint getSmallTickPaint() {
        return mSmallTickPaint;
    }

    public BinaryPaint getBigTickPaint() {
        return mBigTickPaint;
    }


    public void DrawHour(Canvas canvas, Vector2 start, Vector2 end) {
        canvas.drawLine(start.x, start.y, end.x, end.y, mHourPaint);
    }

    public void DrawMinute(Canvas canvas, Vector2 start, Vector2 end) {
        canvas.drawLine(start.x, start.y, end.x, end.y, mMinutePaint);
    }

    public void DrawSecond(Canvas canvas, Vector2 start, Vector2 end) {
        canvas.drawLine(start.x, start.y, end.x, end.y, mSecondPaint);
    }

    /**
     * Updates watch-hand styles based on if we're ambient
     *
     * @param isAmbient
     */
    public void updateWatchHandStyles(boolean isAmbient) {
        for (BinaryPaint paint : new BinaryPaint[]{mHourPaint, mMinutePaint, mSecondPaint}) {
            paint.setActive(!isAmbient);
        }
    }
}
