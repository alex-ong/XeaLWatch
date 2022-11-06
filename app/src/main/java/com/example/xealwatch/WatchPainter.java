package com.example.xealwatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;


public class WatchPainter {

    public Vector2 center = new Vector2(0, 0);
    //Constants defining hand lengths
    private static final float HOUR_HAND_LENGTH = 0.7f;
    private static final float MINUTE_HAND_LENGTH = 0.8f;
    private static final float SECOND_HAND_LENGTH = 0.9f;
    private static final float SECOND_HAND_LENGTH2 = 0.2f;
    private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 8f;

    private static final int NUM_SECONDS = 60;

    //Concrete hand lengths in pixels
    private float mSecondHandLength; //regular length
    private float mSecondHandLength2; //reverse, "overshoot" length
    private float mMinuteHandLength;
    private float mHourHandLength;

    private Bitmap mRawBackground = null;
    private Bitmap mRawGreyBackground = null;
    private Bitmap mCachedBackground = null;
    private Bitmap mCachedGreyBackground = null;
    private Bitmap mCachedBlackBackground = null;
    private final Paint mBlackPaint = new Paint();

    public WatchPainter() {
        mBlackPaint.setColor(Color.BLACK);
    }

    /**
     * Create a few bitmaps with the background and ticks draw on
     *
     * @param backgroundBitmap
     * @param greyBackgroundBitmap
     */
    public void cacheBackgrounds(Bitmap backgroundBitmap, Bitmap greyBackgroundBitmap, PaintBucket paint) {
        //Generate cached backgrounds
        mRawBackground = backgroundBitmap;
        mRawGreyBackground = greyBackgroundBitmap;
        mCachedBackground = generateCachedBackground(backgroundBitmap, paint);
        mCachedGreyBackground = generateCachedBackground(greyBackgroundBitmap, paint);
        mCachedBlackBackground = generateCachedBackground(null, paint);
    }

    private Bitmap generateCachedBackground(Bitmap backgroundImage, PaintBucket paint) {
        int width = (int) center.x * 2;
        int height = (int) center.y * 2;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas writingCanvas = new Canvas(result);
        if (backgroundImage == null)
            writingCanvas.drawColor(Color.BLACK);
        else
            writingCanvas.drawBitmap(backgroundImage, 0, 0, mBlackPaint);
        drawTicks(writingCanvas, new ChargingStatus(), paint);
        return result;
    }

    /*
     * Draw ticks onto a given canvas.
     * Usually you will want to bake this directly into the photo, but in
     * cases where you want to allow users to select their own photos, this dynamically
     * creates them on top of the photo.
     *
     * This will also color the ticks in green based on charging status.
     */
    private void drawTicks(Canvas canvas, ChargingStatus mChargingStatus, PaintBucket mPaintBucket) {
        float innerTickRadius = center.x - 20;
        float innerBigTickRadius = center.x - 25;
        float outerTickRadius = center.x;

        // Set ticks to green if we're charging.
        int stopChargingIndex = Math.round(mChargingStatus.percent / 100f * NUM_SECONDS);
        if (!mChargingStatus.isCharging) stopChargingIndex = 0;
        BinaryPaint mBigTickPaint = mPaintBucket.getBigTickPaint();
        BinaryPaint mSmallTickPaint = mPaintBucket.getSmallTickPaint();
        mBigTickPaint.setActive();
        mSmallTickPaint.setActive();

        for (int tickIndex = 0; tickIndex < NUM_SECONDS; tickIndex++) {
            if (tickIndex == stopChargingIndex) {
                mBigTickPaint.setInactive();
                mSmallTickPaint.setInactive();
            }
            boolean isMajor = tickIndex % 5 == 0;
            float tickRotationDegrees = (float) (tickIndex * (360 / NUM_SECONDS));
            float inner = isMajor ? innerBigTickRadius : innerTickRadius;
            Vector2 innerPos = RotateCoordinate(tickRotationDegrees, inner);
            Vector2 outerPos = RotateCoordinate(tickRotationDegrees, outerTickRadius);
            BinaryPaint paint = isMajor ? mBigTickPaint : mSmallTickPaint;
            canvas.drawLine(center.x + innerPos.x, center.y + innerPos.y,
                    center.x + outerPos.x, center.x + outerPos.y, paint);
        }
    }

    /**
     * Returns a coordinate rotated around the circle.
     *
     * @param rotationDegrees how many degrees to rotate
     * @param distance        distance from centre
     * @return a vector2 representing a point rotated around 0,0
     */
    private Vector2 RotateCoordinate(float rotationDegrees, float distance) {
        rotationDegrees -= 90;
        rotationDegrees = (float) Math.toRadians(rotationDegrees);
        return new Vector2((float) Math.cos(rotationDegrees) * distance,
                (float) Math.sin(rotationDegrees) * distance);
    }

    /**
     * Draws the background, including ticks.
     *
     * @param canvas
     * @param ws
     * @param chargeStatus
     * @param paintBucket
     */
    public void drawBackground(Canvas canvas, WatchState ws, PaintBucket paintBucket, ChargingStatus chargeStatus) {
        // Draw the raw background, then draw the ticks manually
        if (chargeStatus.isCharging) {
            if (ws == WatchState.BLACK) {
                canvas.drawColor(Color.BLACK);
            } else if (ws == WatchState.GRAY) {
                canvas.drawBitmap(mRawGreyBackground, 0, 0, mBlackPaint);
            } else {
                canvas.drawBitmap(mRawBackground, 0, 0, mBlackPaint);
            }
            drawTicks(canvas, chargeStatus, paintBucket);
        } else { // draw the background including ticks from cache.
            if (ws == WatchState.BLACK) {
                canvas.drawBitmap(mCachedBlackBackground, 0, 0, mBlackPaint);
            } else if (ws == WatchState.GRAY) {
                canvas.drawBitmap(mCachedGreyBackground, 0, 0, mBlackPaint);
            } else {
                canvas.drawBitmap(mCachedBackground, 0, 0, mBlackPaint);
            }
        }
    }

    /**
     * Draws the hands and date
     *
     * @param canvas
     * @param mPaintBucket
     * @param calendar
     * @param isAmbient
     */
    public void drawWatchFace(Canvas canvas, PaintBucket mPaintBucket, Calendar calendar, boolean isAmbient) {
        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float secondsRotation = TimeDegrees.GetDegreesValue(Calendar.SECOND, calendar);
        final float minutesRotation = TimeDegrees.GetDegreesValue(Calendar.MINUTE, calendar);
        final float hoursRotation = TimeDegrees.GetDegreesValue(Calendar.HOUR, calendar);

        Vector2 hourStart = RotateCoordinate(hoursRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
        Vector2 hourEnd = RotateCoordinate(hoursRotation, mHourHandLength);

        Vector2 minuteStart = RotateCoordinate(minutesRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
        Vector2 minuteEnd = RotateCoordinate(minutesRotation, mMinuteHandLength);

        Vector2 secondStart = RotateCoordinate(180 + secondsRotation, mSecondHandLength2);
        Vector2 secondEnd = RotateCoordinate(secondsRotation, mSecondHandLength);

        //Add offset to all positions.
        for (Vector2 vec : new Vector2[]{hourStart, hourEnd, minuteStart, minuteEnd, secondStart, secondEnd}) {
            vec.addOffset(center);
        }

        mPaintBucket.DrawHour(canvas, hourStart, hourEnd);
        mPaintBucket.DrawMinute(canvas, minuteStart, minuteEnd);
        if (!isAmbient)
            mPaintBucket.DrawSecond(canvas, secondStart, secondEnd);

        // center circle
        canvas.drawCircle(
                center.x,
                center.y,
                CENTER_GAP_AND_CIRCLE_RADIUS,
                mPaintBucket.getSecondPaint());

    }

    /**
     * Call this when the screen resolution changes.
     *
     * @param width
     * @param height
     */
    public void updateSurface(int width, int height) {
        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
        center.x = width / 2f;
        center.y = height / 2f;

        /*
         * Calculate lengths of different hands based on watch screen size.
         */
        mSecondHandLength = center.x * SECOND_HAND_LENGTH;
        mSecondHandLength2 = center.x * SECOND_HAND_LENGTH2;
        mMinuteHandLength = center.x * MINUTE_HAND_LENGTH;
        mHourHandLength = center.x * HOUR_HAND_LENGTH;
    }


}
