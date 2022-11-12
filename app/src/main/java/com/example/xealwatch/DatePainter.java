package com.example.xealwatch;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Calendar;

public class DatePainter {
    final PaintBucket mPaintBucket;
    final Vector2 mDateCenter = new Vector2();
    final int height = 18;
    final int width = 20;
    final int heightOffset = 50;

    public DatePainter(PaintBucket paintBucket) {
        mPaintBucket = paintBucket;
    }

    public void OnCanvasChange(int width, int height) {
        mDateCenter.x = width / 2f;
        mDateCenter.y = height - heightOffset;
    }

    public void DrawDate(Canvas canvas, int date) {
        BinaryPaint datePaint = mPaintBucket.getDatePaint();
        BinaryPaint dateInsetPaint = mPaintBucket.getDateInsetPaint();
        BinaryPaint dateTextPaint = mPaintBucket.getDateTextPaint();

        Rect box = new Rect((int) mDateCenter.x - width,
                (int) mDateCenter.y - height,
                (int) mDateCenter.x + width,
                (int) mDateCenter.y + height);
        canvas.drawRect(box, datePaint);
        box.left += 1;
        box.top += 1;
        box.bottom -= 1;
        box.right -= 1;
        canvas.drawRect(box, dateInsetPaint);

        String dayOfMonth = String.valueOf(date);
        Rect result = new Rect();
        dateTextPaint.getTextBounds(dayOfMonth, 0, dayOfMonth.length(), result);
        canvas.drawText(dayOfMonth, mDateCenter.x, mDateCenter.y + result.height() / 2f, dateTextPaint);
    }
}
