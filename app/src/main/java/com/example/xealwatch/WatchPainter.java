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


    private final Paint mBlackPaint = new Paint();
    private final CachedBackgrounds backgrounds = new CachedBackgrounds();
    private final DatePainter mDatePainter;

    public WatchPainter(PaintBucket paintBucket) {
        mBlackPaint.setColor(Color.BLACK);
        mPaintBucket = paintBucket;
        mDatePainter = new DatePainter(mPaintBucket);
    }

    private static class TickData {
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
    public void cacheBackgrounds(Bitmap backgroundBitmap, Bitmap greyBackgroundBitmap, int date) {
        //Generate cached backgrounds
        backgrounds.setRawBackground(backgroundBitmap);
        backgrounds.setRawGreyBackground(greyBackgroundBitmap);
        regenerateBackgrounds(date);
    }

    /**
     * Generate the color, grey and black backgrounds based on the date.
     *
     * @param date current date.
     */
    private void regenerateBackgrounds(int date) {
        Bitmap backgroundBitmap = backgrounds.getRawBackground();
        Bitmap greyBackgroundBitmap = backgrounds.getRawGreyBackground();
        Bitmap color = generateCachedBackground(backgroundBitmap, WatchState.FULL, date);
        Bitmap greyscale = generateCachedBackground(greyBackgroundBitmap, WatchState.GRAY, date);
        Bitmap black = generateCachedBackground(null, WatchState.BLACK, date);
        backgrounds.setCachedBackgrounds(color, greyscale, black, date);
    }

    /* generates a cached background by drawing the raw bitmap, and then the ticks and date */
    private Bitmap generateCachedBackground(Bitmap backgroundImage, WatchState ws, int date) {
        int width = (int) center.x * 2;
        int height = (int) center.y * 2;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas writingCanvas = new Canvas(result);
        if (backgroundImage == null)
            writingCanvas.drawColor(Color.BLACK);
        else
            writingCanvas.drawBitmap(backgroundImage, 0, 0, mBlackPaint);
        drawTicks(writingCanvas, new ChargingStatus(), ws);
        mDatePainter.DrawDate(writingCanvas, date);
        return result;
    }

    /* Returns which index to stop charging at */
    private int getStopChargingIndex(ChargingStatus chargingStatus) {
        if (!chargingStatus.isCharging) return 0;
        return Math.round(chargingStatus.percent / 100f * NUM_SECONDS);
    }

    /* Draws a single tick */
    private void drawTick(Canvas canvas, float tickRadius, float endRadius, int tickIndex, BinaryPaint paint) {
        float tickRotationDegrees = (float) (tickIndex * 360 / NUM_SECONDS);
        Vector2 innerPos = rotateCoordinate(tickRotationDegrees, tickRadius);
        Vector2 outerPos = rotateCoordinate(tickRotationDegrees, endRadius);
        canvas.drawLine(innerPos.x, innerPos.y, outerPos.x, outerPos.y, paint);
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
        int stopChargingIndex = getStopChargingIndex(chargingStatus);
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
     * Draws the Twelve o'clock symbol using two lines.
     *
     * @param canvas      canvas
     * @param drawBlack   whether to overlay in black
     * @param paintBucket paintBucket to draw with
     */
    private void drawTwelveOClock(Canvas canvas, boolean drawBlack, PaintBucket paintBucket) {
        Vector2 innerPos = rotateCoordinate(0, tickData.bigTickRadius - 10);
        Vector2 outerPos = rotateCoordinate(0, center.x);
        drawThickTick(canvas, paintBucket.getBigTickPaint(), innerPos, outerPos, 0, 1);

        // overdraw a black radius
        if (drawBlack) {
            innerPos = rotateCoordinate(0, tickData.bigInsetRadius - 10);
            outerPos = rotateCoordinate(0, center.x);
            drawThickTick(canvas, paintBucket.getBigTickInsetPaint(), innerPos, outerPos, -1, 3);
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
    private void drawThickTick(Canvas canvas, BinaryPaint paint, Vector2 innerPos, Vector2 outerPos, float leftOffset, float rightOffset) {
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
    private Vector2 rotateCoordinate(float rotationDegrees, float distance) {
        rotationDegrees -= 90;
        rotationDegrees = (float) Math.toRadians(rotationDegrees);
        Vector2 result = new Vector2((float) Math.cos(rotationDegrees) * distance,
                (float) Math.sin(rotationDegrees) * distance);
        result.addOffset(center);
        return result;
    }

    /**
     * Draws the background, including ticks and date
     *
     * @param canvas       Canvas to draw on
     * @param ws           the watch's watch-state
     * @param chargeStatus if the watch is charging
     */
    public void drawBackground(Canvas canvas, WatchState ws, ChargingStatus chargeStatus, int date) {

        // Draw the raw background, then draw the ticks manually
        if (chargeStatus.isCharging) {
            backgrounds.drawRawBackground(canvas, ws);
            drawTicks(canvas, chargeStatus, ws);
            mDatePainter.DrawDate(canvas, date);
        } else { // draw the background including ticks from cache.
            if (backgrounds.cacheRequireRebuild(date)) {
                regenerateBackgrounds(date);
            }
            backgrounds.drawBackground(canvas, ws);
        }
    }

    /**
     * Draws the hands and date
     *
     * @param canvas   Canvas to draw on
     * @param calendar Current Date/Time
     * @param ws       Current watchState
     */
    public void drawWatchFace(Canvas canvas, Calendar calendar, WatchState ws) {
        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float secondsRotation = TimeDegrees.GetDegreesValue(Calendar.SECOND, calendar);
        final float minutesRotation = TimeDegrees.GetDegreesValue(Calendar.MINUTE, calendar);
        final float hoursRotation = TimeDegrees.GetDegreesValue(Calendar.HOUR, calendar);

        Vector2 hourStart = rotateCoordinate(hoursRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
        Vector2 hourEnd = rotateCoordinate(hoursRotation, mHourHandLength);

        Vector2 minuteStart = rotateCoordinate(minutesRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
        Vector2 minuteEnd = rotateCoordinate(minutesRotation, mMinuteHandLength);

        Vector2 secondStart = rotateCoordinate(180 + secondsRotation, mSecondHandLength2);
        Vector2 secondEnd = rotateCoordinate(secondsRotation, mSecondHandLength);

        DrawLine(canvas, hourStart, hourEnd, mPaintBucket.getHourPaint());
        DrawLine(canvas, minuteStart, minuteEnd, mPaintBucket.getMinutePaint());
        if (ws == WatchState.FULL) {
            DrawLine(canvas, secondStart, secondEnd, mPaintBucket.getSecondPaint());
            canvas.drawCircle(center.x, center.y, CENTER_GAP_AND_CIRCLE_RADIUS, mPaintBucket.getSecondPaint());
        } else { //draw black over the hour and minute hand
            DrawLine(canvas, hourStart, hourEnd, mPaintBucket.getHourInsetPaint());
            DrawLine(canvas, minuteStart, minuteEnd, mPaintBucket.getMinuteInsetPaint());
            canvas.drawCircle(center.x, center.y, CENTER_GAP_AND_CIRCLE_RADIUS, mPaintBucket.getSmallTickPaint());
        }
    }

    /* Draws a single line */
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

        /*
         * Update any dependencies
         */
        mDatePainter.OnCanvasChange(width, height);
    }
}
