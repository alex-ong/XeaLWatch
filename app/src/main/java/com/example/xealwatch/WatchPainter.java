package com.example.xealwatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;


public class WatchPainter {

    private final PaintBucket mPaintBucket;
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

    private final DatePainter mDatePainter;

    public WatchPainter(PaintBucket paintBucket) {
        mBlackPaint.setColor(Color.BLACK);
        mPaintBucket = paintBucket;
        mDatePainter = new DatePainter(mPaintBucket);
    }

    private class TickData {
        public float smallTickRadius;
        public float endSmallTickRadius;
        public float bigTickRadius;
        public float bigInsetRadius;
    }

    private final TickData tickData = new TickData();

    /**
     * Create a few bitmaps with the background and ticks draw on
     *
     * @param backgroundBitmap     background image in full colour
     * @param greyBackgroundBitmap background image in greyscale
     */
    public void cacheBackgrounds(Bitmap backgroundBitmap, Bitmap greyBackgroundBitmap) {
        //Generate cached backgrounds
        mRawBackground = backgroundBitmap;
        mRawGreyBackground = greyBackgroundBitmap;
        mCachedBackground = generateCachedBackground(backgroundBitmap, WatchState.FULL);
        mCachedGreyBackground = generateCachedBackground(greyBackgroundBitmap, WatchState.GRAY);
        mCachedBlackBackground = generateCachedBackground(null, WatchState.BLACK);
    }

    private Bitmap generateCachedBackground(Bitmap backgroundImage, WatchState ws) {
        int width = (int) center.x * 2;
        int height = (int) center.y * 2;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas writingCanvas = new Canvas(result);
        if (backgroundImage == null)
            writingCanvas.drawColor(Color.BLACK);
        else
            writingCanvas.drawBitmap(backgroundImage, 0, 0, mBlackPaint);
        drawTicks(writingCanvas, new ChargingStatus(), ws);
        return result;
    }

    /* Draws a single tick */
    private void drawTick(Canvas canvas, float tickRadius, float endRadius, int tickIndex, BinaryPaint paint) {
        float tickRotationDegrees = (float) (tickIndex * 360 / NUM_SECONDS);
        Vector2 innerPos = RotateCoordinate(tickRotationDegrees, tickRadius);
        Vector2 outerPos = RotateCoordinate(tickRotationDegrees, endRadius);
        canvas.drawLine(innerPos.x, innerPos.y, outerPos.x, outerPos.y, paint);
    }

    /* Returns which index to stop charging at */
    private int GetStopChargingIndex(ChargingStatus chargingStatus) {
        if (!chargingStatus.isCharging) return 0;
        return Math.round(chargingStatus.percent / 100f * NUM_SECONDS);
    }

    /*
     * Draw ticks onto a given canvas.
     * Usually you will want to bake this directly into the photo, but in
     * cases where you want to allow users to select their own photos, this dynamically
     * creates them on top of the photo.
     *
     * This will also color the ticks in green based on charging status.
     */
    private void drawTicks(Canvas canvas, ChargingStatus chargingStatus, WatchState ws) {
        // Set ticks to green if we're charging.
        int stopChargingIndex = GetStopChargingIndex(chargingStatus);
        // Draw black afterwards if we're in ambient.
        boolean drawBlack = ws.ordinal() < WatchState.FULL.ordinal() && !chargingStatus.isCharging;
        mPaintBucket.setTicksActive();

        for (int tickIndex = 0; tickIndex < NUM_SECONDS; tickIndex++) {
            if (tickIndex >= stopChargingIndex) {
                mPaintBucket.setTicksInActive();
            }
            if (tickIndex == 0) {
                drawTwelveOClock(canvas, drawBlack, mPaintBucket);
                continue;
            }
            boolean isMajor = tickIndex % 5 == 0;
            if (isMajor) {
                drawTick(canvas, tickData.bigTickRadius, center.x,
                        tickIndex, mPaintBucket.getBigTickPaint());
                // overdraw a black radius
                if (drawBlack) {
                    drawTick(canvas, tickData.bigInsetRadius, center.x,
                            tickIndex, mPaintBucket.getBigTickInsetPaint());
                }
            } else {
                drawTick(canvas, tickData.smallTickRadius, tickData.endSmallTickRadius,
                        tickIndex, mPaintBucket.getSmallTickPaint());
            }
        }
        // Fix bug where these are left active
        if (stopChargingIndex >= NUM_SECONDS - 1) {
            mPaintBucket.setTicksInActive();
        }
    }

    /**
     * Draws the Twelve oclock symbol using two lines.
     *
     * @param canvas      canvas
     * @param drawBlack   whether to overlay in black
     * @param paintBucket paintBucket to draw with
     */
    private void drawTwelveOClock(Canvas canvas, boolean drawBlack, PaintBucket paintBucket) {
        Vector2 innerPos = RotateCoordinate(0, tickData.bigTickRadius - 10);
        Vector2 outerPos = RotateCoordinate(0, center.x);
        DrawThickTick(canvas, paintBucket.getBigTickPaint(), innerPos, outerPos, -1,1);

        // overdraw a black radius
        if (drawBlack) {
            innerPos = RotateCoordinate(0, tickData.bigInsetRadius - 10);
            outerPos = RotateCoordinate(0, center.x);
            DrawThickTick(canvas, paintBucket.getBigTickInsetPaint(), innerPos, outerPos, -2,3);
        }
    }

    /**
     * Draws two ticks next to each other.
     *
     * @param canvas      canvas to draw on
     * @param paint       paint to draw with
     * @param innerPos    start of line
     * @param outerPos    end of line
     * @param rightOffset adds an additional few pixels to the right tick
     */
    private void DrawThickTick(Canvas canvas, BinaryPaint paint, Vector2 innerPos, Vector2 outerPos, float leftOffset, float rightOffset) {
        float tickWidth = paint.getStrokeWidth();
        innerPos.x -= tickWidth * 0.5 - leftOffset;
        outerPos.x -= tickWidth * 0.5 - leftOffset;
        canvas.drawLine(innerPos.x, innerPos.y, outerPos.x, outerPos.y, paint);
        innerPos.x += tickWidth + rightOffset;
        outerPos.x += tickWidth + rightOffset;
        canvas.drawLine(innerPos.x, innerPos.y, outerPos.x, outerPos.y, paint);
    }

    /**
     * Returns a coordinate rotated around the circle.
     *
     * @param rotationDegrees how many degrees to rotate
     * @param distance        distance from centre
     * @return a vector2 representing a point rotated around the center of the screen.
     */
    private Vector2 RotateCoordinate(float rotationDegrees, float distance) {
        rotationDegrees -= 90;
        rotationDegrees = (float) Math.toRadians(rotationDegrees);
        Vector2 result = new Vector2((float) Math.cos(rotationDegrees) * distance,
                (float) Math.sin(rotationDegrees) * distance);
        result.addOffset(center);
        return result;
    }

    /**
     * Draws the background, including ticks.
     *
     * @param canvas       Canvas to draw on
     * @param ws           the watch's watch-state
     * @param chargeStatus if the watch is charging
     */
    public void drawBackground(Canvas canvas, WatchState ws, ChargingStatus chargeStatus) {
        // Draw the raw background, then draw the ticks manually
        if (chargeStatus.isCharging) {
            if (ws == WatchState.BLACK) {
                canvas.drawColor(Color.BLACK);
            } else if (ws == WatchState.GRAY) {
                canvas.drawBitmap(mRawGreyBackground, 0, 0, mBlackPaint);
            } else {
                canvas.drawBitmap(mRawBackground, 0, 0, mBlackPaint);
            }
            drawTicks(canvas, chargeStatus, ws);
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
     * @param canvas      Canvas to draw on
     * @param calendar    Current Date/Time
     * @param ws          Current watchState
     */
    public void drawWatchFace(Canvas canvas, Calendar calendar, WatchState ws) {
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

        DrawLine(canvas, hourStart, hourEnd, mPaintBucket.getHourPaint());
        DrawLine(canvas, minuteStart, minuteEnd, mPaintBucket.getMinutePaint());
        if (ws == WatchState.FULL) {
            DrawLine(canvas, secondStart, secondEnd, mPaintBucket.getSecondPaint());
            canvas.drawCircle(center.x, center.y, CENTER_GAP_AND_CIRCLE_RADIUS, mPaintBucket.getSecondPaint());
        } else { //draw black over the hour and minute hand
            DrawLine(canvas, hourStart,hourEnd, mPaintBucket.getHourInsetPaint());
            DrawLine(canvas, minuteStart, minuteEnd, mPaintBucket.getMinuteInsetPaint());
            canvas.drawCircle(center.x, center.y, CENTER_GAP_AND_CIRCLE_RADIUS, mPaintBucket.getSmallTickPaint());
        }
    }

    private void DrawLine(Canvas canvas, Vector2 start, Vector2 end, BinaryPaint paint) {
        canvas.drawLine(start.x, start.y, end.x, end.y, paint);
    }

    /**
     * Call this when the screen resolution changes.
     *
     * @param width  width of screen
     * @param height height of screen
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

        tickData.smallTickRadius = center.x - 17;
        tickData.endSmallTickRadius = center.x - 10;
        tickData.bigTickRadius = center.x - 25;
        tickData.bigInsetRadius = center.x - 24;
    }
}
