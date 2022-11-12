package com.example.xealwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

public class PaintBucket {
    private final BinaryPaint mHourPaint;
    private final BinaryPaint mMinutePaint;
    private final BinaryPaint mSecondPaint;

    private final BinaryPaint mHourInsetPaint;
    private final BinaryPaint mMinuteInsetPaint;

    private final BinaryPaint mSmallTickPaint;
    private final BinaryPaint mBigTickPaint;
    private final BinaryPaint mBigTickInsetPaint;

    private final BinaryPaint mDatePaint;
    private final BinaryPaint mDateInsetPaint;
    private final BinaryPaint mDateTextPaint;

    private static final float HOUR_STROKE_WIDTH = 15f;
    private static final float MINUTE_STROKE_WIDTH = 10f;
    private static final float SECOND_STROKE_WIDTH = 5f;
    private static final float LARGE_SECOND_TICK_STROKE_WIDTH = 16f;
    private static final float SMALL_SECOND_TICK_STROKE_WIDTH = 2f;
    public PaintBucket(int watchHandColor, int watchHandSecondColor, int watchTickColor) {
        mHourPaint = new BinaryPaint();
        mHourPaint.initializeActive(watchHandColor, HOUR_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mMinutePaint = new BinaryPaint();
        mMinutePaint.initializeActive(watchHandColor, MINUTE_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mHourInsetPaint = new BinaryPaint();
        mHourInsetPaint.initializeActive(Color.BLACK, (HOUR_STROKE_WIDTH - 2), Paint.Cap.ROUND, Paint.Style.FILL);

        mMinuteInsetPaint = new BinaryPaint();
        mMinuteInsetPaint.initializeActive(Color.BLACK, (MINUTE_STROKE_WIDTH - 2), Paint.Cap.ROUND, Paint.Style.FILL);

        mSecondPaint = new BinaryPaint();
        mSecondPaint.initializeActive(watchHandSecondColor, SECOND_STROKE_WIDTH, Paint.Cap.ROUND, Paint.Style.FILL);

        mSmallTickPaint = new BinaryPaint();
        mSmallTickPaint.initializeActive(watchTickColor, SMALL_SECOND_TICK_STROKE_WIDTH, Paint.Cap.BUTT, Paint.Style.STROKE);

        mBigTickPaint = new BinaryPaint();
        mBigTickPaint.initializeActive(watchTickColor, LARGE_SECOND_TICK_STROKE_WIDTH, Paint.Cap.BUTT, Paint.Style.STROKE);
        mBigTickPaint.initializeInactive(Color.WHITE, Paint.Style.STROKE);

        mBigTickInsetPaint = new BinaryPaint();
        mBigTickInsetPaint.initializeActive(watchTickColor, (LARGE_SECOND_TICK_STROKE_WIDTH - 2), Paint.Cap.BUTT, Paint.Style.STROKE);
        mBigTickInsetPaint.initializeInactive(Color.BLACK, Paint.Style.STROKE);

        mDateInsetPaint = new BinaryPaint();
        mDateInsetPaint.initializeActive(Color.BLACK, 2, Paint.Cap.BUTT, Paint.Style.FILL);

        mDatePaint = new BinaryPaint();
        mDatePaint.initializeActive(Color.WHITE, 1, Paint.Cap.BUTT, Paint.Style.FILL);

        mDateTextPaint = new BinaryPaint();
        mDateTextPaint.initializeActive(Color.WHITE, 2, Paint.Cap.BUTT, Paint.Style.FILL);
        mDateTextPaint.setTextSize(30);

        mDateTextPaint.setTextAlign(Paint.Align.CENTER);

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

    public BinaryPaint getHourInsetPaint() {
        return mHourInsetPaint;
    }

    public BinaryPaint getMinuteInsetPaint() {
        return mMinuteInsetPaint;
    }

    public BinaryPaint getBigTickPaint() {
        return mBigTickPaint;
    }

    public BinaryPaint getBigTickInsetPaint() {
        return mBigTickInsetPaint;
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

    public void DrawHourInset(Canvas canvas, Vector2 start, Vector2 end) {
        canvas.drawLine(start.x, start.y, end.x, end.y, mHourInsetPaint);
    }

    public void DrawMinuteInset(Canvas canvas, Vector2 start, Vector2 end) {
        canvas.drawLine(start.x, start.y, end.x, end.y, mMinuteInsetPaint);
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

    /**
     * Reduces alpha of the hands when we are in mute mode
     *
     * @param inMuteMode whether to mute the colors
     */
    public void setMuteMode(boolean inMuteMode) {
        mHourPaint.setAlpha(inMuteMode ? 100 : 255);
        mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
        mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
    }

    public void setTicksInActive() {
        mBigTickPaint.setInactive();
        mBigTickInsetPaint.setInactive();
        mSmallTickPaint.setInactive();
    }

    public void setTicksActive() {
        mBigTickPaint.setActive();
        mBigTickInsetPaint.setActive();
        mSmallTickPaint.setActive();
    }

    public BinaryPaint getDatePaint() {
        return mDatePaint;
    }

    public BinaryPaint getDateInsetPaint() {
        return mDateInsetPaint;
    }

    public BinaryPaint getDateTextPaint() {
        return mDateTextPaint;
    }
}
