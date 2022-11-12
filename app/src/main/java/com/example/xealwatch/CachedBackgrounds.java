package com.example.xealwatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class CachedBackgrounds {
    private Bitmap mRawBackground = null;
    private Bitmap mRawGreyBackground = null;
    private Bitmap mCachedBackground = null;
    private Bitmap mCachedGreyBackground = null;
    private Bitmap mCachedBlackBackground = null;

    private int mDate = -1;
    private final Paint mBlackPaint = new Paint();

    public boolean cacheRequireRebuild(int date)
    {
        return (date != mDate);
    }

    /**
     * Constructor
     */
    public CachedBackgrounds() {
        mBlackPaint.setColor(Color.BLACK);
    }

    public Bitmap getRawBackground() {
        return mRawBackground;
    }

    public void setRawBackground(Bitmap rawBackground) {
        mRawBackground = rawBackground;
    }

    public Bitmap getRawGreyBackground() {
        return mRawGreyBackground;
    }

    public void setRawGreyBackground(Bitmap mRawGreyBackground) {
        this.mRawGreyBackground = mRawGreyBackground;
    }

    public Bitmap getCachedBackground() {
        return mCachedBackground;
    }

    public void setCachedBackground(Bitmap mCachedBackground) {
        this.mCachedBackground = mCachedBackground;
    }

    public Bitmap getCachedGreyBackground() {
        return mCachedGreyBackground;
    }

    public void setCachedGreyBackground(Bitmap mCachedGreyBackground) {
        this.mCachedGreyBackground = mCachedGreyBackground;
    }

    public Bitmap getCachedBlackBackground() {
        return mCachedBlackBackground;
    }

    public void setCachedBlackBackground(Bitmap mCachedBlackBackground) {
        this.mCachedBlackBackground = mCachedBlackBackground;
    }


    public void setCachedBackgrounds(Bitmap color, Bitmap greyscale, Bitmap black, int date) {
        mCachedBackground = color;
        mCachedGreyBackground = greyscale;
        mCachedBlackBackground = black;
        mDate = date;
    }


    /**
     * return completed background based on WatchState.
     * @param ws The state of the watch
     * @return
     */
    public Bitmap getBackground(WatchState ws) {
        if (ws == WatchState.BLACK) return mCachedBlackBackground;
        if (ws == WatchState.GRAY) return mCachedGreyBackground;
        return mCachedBackground;
    }

    public Bitmap getRawBackground(WatchState ws) {
        if (ws == WatchState.BLACK) return null;
        if (ws == WatchState.GRAY) return mRawGreyBackground;
        return mRawBackground;
    }

    /**
     * Draw background based on WatchState.
     * @param canvas canvas to draw on
     * @param ws watchstate
     */
    public void drawBackground(Canvas canvas, WatchState ws)
    {
        Bitmap background = getBackground(ws);
        canvas.drawBitmap(background,0,0, mBlackPaint);
    }

    /***
     * Draw raw background onto canvas based on watchstate.
     * @param canvas canvas to draw on
     * @param ws watchstate
     */
    public void drawRawBackground(Canvas canvas, WatchState ws)
    {
        Bitmap background = getBackground(ws);
        if (background != null) {
            canvas.drawBitmap(background, 0, 0, mBlackPaint);
        }
        else
        {
            canvas.drawColor(Color.BLACK);
        }
    }
}
