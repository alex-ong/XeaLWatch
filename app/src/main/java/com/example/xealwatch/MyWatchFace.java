package com.example.xealwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.palette.graphics.Palette;

import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn"t
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {

    /**
     * Updates rate in milliseconds for interactive mode. We update at 60FPS
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = 33;

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final float HOUR_STROKE_WIDTH = 15f;
        private static final float MINUTE_STROKE_WIDTH = 10f;
        private static final float SECOND_STROKE_WIDTH = 5f;
        private static final float SMALL_SECOND_TICK_STROKE_WIDTH = 2f;

        private static final float HOUR_HAND_LENGTH = 0.7f;
        private static final float MINUTE_HAND_LENGTH = 0.9f;
        private static final float SECOND_HAND_LENGTH = 0.95f;
        private static final float SECOND_HAND_LENGTH2 = 0.2f;

        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 8f;

        private static final int NUM_SECONDS = 60;

        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private ChargingStatus mChargingStatus = new ChargingStatus();
        private final BroadcastReceiver mChargingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mChargingStatus.SetBatteryStatusIntent(intent);
                mChargingStatus.Update();
                invalidate();
            }
        };

        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        private float mCenterX;
        private float mCenterY;
        private float mSecondHandLength; //regular length
        private float mSecondHandLength2; //reverse, "overshoot" length
        private float mMinuteHandLength;
        private float mHourHandLength;
        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mWatchHandColor = Color.WHITE;
        private int mWatchHandSecondColor = Color.RED;
        private int mWatchTickColor = Color.GREEN; // green while charging.

        private BinaryPaint mHourPaint;
        private BinaryPaint mMinutePaint;
        private BinaryPaint mSecondPaint;
        private BinaryPaint mSmallTickPaint;
        private BinaryPaint mBigTickPaint;

        private BinaryPaint mBackgroundPaint;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            initializeBackground();
            initializeWatchFace();
            System.out.println("onCreate");
        }

        private void initializeBackground() {
            mBackgroundPaint = new BinaryPaint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watchface_service_bg);
        }

        /**
         * Call this to automatically set our hand colors based on bg image.
         */
        private void setAutoHandColor()
        {
            /* Extracts colors from background image to improve watchface style. */
            Palette.from(mBackgroundBitmap).generate(palette -> {
                if (palette != null) {
                    mWatchHandSecondColor = palette.getVibrantColor(Color.RED);
                    mWatchHandColor = palette.getLightVibrantColor(Color.WHITE);
                    updateWatchHandStyle();
                }
            });
        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mHourPaint = new BinaryPaint();
            mHourPaint.initializeActive(mWatchHandColor,HOUR_STROKE_WIDTH,Paint.Cap.ROUND, Paint.Style.FILL);

            mMinutePaint = new BinaryPaint();
            mMinutePaint.initializeActive(mWatchHandColor,MINUTE_STROKE_WIDTH,Paint.Cap.ROUND,Paint.Style.FILL);

            mSecondPaint = new BinaryPaint();
            mSecondPaint.initializeActive(mWatchHandSecondColor,SECOND_STROKE_WIDTH,Paint.Cap.ROUND,Paint.Style.FILL);

            mSmallTickPaint = new BinaryPaint();
            mSmallTickPaint.initializeActive(mWatchTickColor,SMALL_SECOND_TICK_STROKE_WIDTH,Paint.Cap.BUTT,Paint.Style.STROKE);

            mBigTickPaint = new BinaryPaint();
            mBigTickPaint.initializeActive(mWatchTickColor,SECOND_STROKE_WIDTH,Paint.Cap.BUTT,Paint.Style.STROKE);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;
            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        /**
         * Updates the paint for hour,minute,second based on whether we are in ambient mode
         */
        private void updateWatchHandStyle() {
            BinaryPaint[] paints = new BinaryPaint[]{ mHourPaint, mMinutePaint, mSecondPaint};
            for (BinaryPaint p : paints) {
                p.setActive(!mAmbient);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * SECOND_HAND_LENGTH);
            mSecondHandLength2 = (float) (mCenterX * SECOND_HAND_LENGTH2);
            mMinuteHandLength = (float) (mCenterX * MINUTE_HAND_LENGTH);
            mHourHandLength = (float) (mCenterX * HOUR_HAND_LENGTH);

            /* Scale loaded background image (more efficient) if surface dimensions change. */
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * scale),
                    (int) (mBackgroundBitmap.getHeight() * scale), true);

            /*
             * Create a gray version of the image only if it will look nice on the device in
             * ambient mode. That means we don't want devices that support burn-in
             * protection (slight movements in pixels, not great for images going all the way to
             * edges) and low ambient mode (degrades image quality).
             *
             * Also, if your watch face will know about all images ahead of time (users aren"t
             * selecting their own photos for the watch face), it will be more
             * efficient to create a black/white version (png, etc.) and load that when you need it.
             */
            if (!mBurnInProtection && !mLowBitAmbient) {
                initGrayBackgroundBitmap();
            }
        }

        private void initGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new BinaryPaint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            drawBackground(canvas);
            drawWatchFace(canvas);
        }

        private void drawBackground(Canvas canvas) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, 0, mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
            }
        }

        /*
         * Draw ticks. Usually you will want to bake this directly into the photo, but in
         * cases where you want to allow users to select their own photos, this dynamically
         * creates them on top of the photo.
         *
         * This will also color the ticks in green based on charging status.
         */
        private void drawTicks(Canvas canvas)
        {
            float innerTickRadius = mCenterX - 15;
            float innerBigTickRadius = mCenterX - 20;
            float outerTickRadius = mCenterX;

            // Set ticks to green if we're charging.
            int stopChargingIndex = Math.round(mChargingStatus.percent/100f * NUM_SECONDS);
            if (!mChargingStatus.isCharging) stopChargingIndex = 0;
            mBigTickPaint.setActive();
            mSmallTickPaint.setActive();

            for (int tickIndex = 0; tickIndex < NUM_SECONDS; tickIndex++) {
                if (tickIndex == stopChargingIndex)
                {
                    mBigTickPaint.setInactive();
                    mSmallTickPaint.setInactive();
                }
                boolean isMajor = tickIndex % 5 == 0;
                float tickRotationDegrees = (float) (tickIndex * (360 / NUM_SECONDS));
                float inner = isMajor ? innerBigTickRadius : innerTickRadius;
                Vector2 innerPos = RotateCoordinate(tickRotationDegrees, inner);
                Vector2 outerPos = RotateCoordinate(tickRotationDegrees, outerTickRadius);
                BinaryPaint paint = isMajor ? mBigTickPaint : mSmallTickPaint;
                canvas.drawLine(mCenterX + innerPos.x, mCenterY + innerPos.y,
                                mCenterX + outerPos.x, mCenterY + outerPos.y, paint);
            }
        }

        /**
         * Returns a coordinate rotated around the circle.
         * @param rotationDegrees
         * @param distance
         * @return
         */
        private Vector2 RotateCoordinate(float rotationDegrees, float distance)
        {
            rotationDegrees -= 90;
            rotationDegrees = (float) Math.toRadians(rotationDegrees);
            return new Vector2((float) Math.cos(rotationDegrees) * distance,
                               (float) Math.sin(rotationDegrees) * distance);
        }



        private void drawWatchFace(Canvas canvas) {
            drawTicks(canvas);

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float secondsRotation = TimeDegrees.GetDegreesValue(Calendar.SECOND, mCalendar);
            final float minutesRotation = TimeDegrees.GetDegreesValue(Calendar.MINUTE, mCalendar);
            final float hoursRotation = TimeDegrees.GetDegreesValue(Calendar.HOUR, mCalendar);

            Vector2 hourStart = RotateCoordinate(hoursRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
            Vector2 hourEnd = RotateCoordinate(hoursRotation, mHourHandLength);

            Vector2 minuteStart = RotateCoordinate(minutesRotation, CENTER_GAP_AND_CIRCLE_RADIUS);
            Vector2 minuteEnd = RotateCoordinate(minutesRotation, mMinuteHandLength);

            Vector2 secondStart = RotateCoordinate(180 + secondsRotation, mSecondHandLength2);
            Vector2 secondEnd = RotateCoordinate(secondsRotation, mSecondHandLength);

            canvas.drawLine(hourStart.x + mCenterX,
                            hourStart.y + mCenterY,
                            hourEnd.x + mCenterX,
                            hourEnd.y + mCenterY, mHourPaint);

            canvas.drawLine(minuteStart.x + mCenterX,
                            minuteStart.y + mCenterY,
                            minuteEnd.x + mCenterX,
                            minuteEnd.y + mCenterY, mMinutePaint);
            if (!mAmbient) {
                canvas.drawLine(secondStart.x + mCenterX,
                                secondStart.y + mCenterY,
                                secondEnd.x + mCenterX,
                                secondEnd.y + mCenterY, mSecondPaint);
            }

            canvas.drawCircle(
                    mCenterX,
                    mCenterY,
                    CENTER_GAP_AND_CIRCLE_RADIUS,
                    mSecondPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerTimeZoneReceiver();
                registerChargingReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterTimeZoneReceiver();
                unregisterChargingReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }


        private void registerChargingReceiver()
        {
            if (mChargingStatus.receiverRegistered) return;
            mChargingStatus.receiverRegistered = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            MyWatchFace.this.registerReceiver(mChargingReceiver, filter);
        }

        private void unregisterChargingReceiver()
        {
            if (!mChargingStatus.receiverRegistered) return;
            mChargingStatus.receiverRegistered = false;
            MyWatchFace.this.unregisterReceiver(mChargingReceiver);
        }

        private void registerTimeZoneReceiver() {
            if (mRegisteredTimeZoneReceiver) return;

            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterTimeZoneReceiver() {
            if (!mRegisteredTimeZoneReceiver) return;

            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}