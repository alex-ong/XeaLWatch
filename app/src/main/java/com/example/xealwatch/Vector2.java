package com.example.xealwatch;

public class Vector2 {
    public float x;
    public float y;

    public Vector2 (float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds an offset to this vector's values
     * @param x
     * @param y
     */
    public void addOffset(float x, float y)
    {
        this.x += x;
        this.y += y;
    }
}
