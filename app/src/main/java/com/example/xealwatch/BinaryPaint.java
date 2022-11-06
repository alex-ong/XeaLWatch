package com.example.xealwatch;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * A paint class with two states.
 */
public class BinaryPaint extends Paint {

    private int activeColor;
    private Style activeStyle;

    private int inactiveColor = Color.WHITE;
    private Style inactiveStyle = Style.STROKE;

    public void initializeActive(int color, float strokeWidth, Paint.Cap strokeCap, Style style) {
        activeColor = color;
        activeStyle = style;
        setColor(activeColor);
        setStrokeWidth(strokeWidth);
        setStrokeCap(strokeCap);
        setAntiAlias(true);
        setStyle(activeStyle);
    }

    public void initializeInactive(int color, Style style) {
        inactiveColor = color;
        inactiveStyle = style;
    }

    /**
     * Sets paint to active settings
     */
    public void setActive() {
        setColor(activeColor);
        setStyle(activeStyle);
    }

    /**
     * Sets paint to inactive settings
     */
    public void setInactive() {
        setColor(inactiveColor);
        setStyle(inactiveStyle);
    }

    /**
     * Boolean method to set active/inactive
     *
     * @param active whether we are active
     */
    public void setActive(boolean active) {
        if (active) setActive();
        else setInactive();
    }
}
