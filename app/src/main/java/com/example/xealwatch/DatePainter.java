package com.example.xealwatch;

import android.graphics.Canvas;
import android.graphics.Rect;

public class DatePainter {
    PaintBucket mPaintBucket;
    Vector2 mDateCenter = new Vector2();

    public DatePainter(PaintBucket paintBucket) {
        mPaintBucket = paintBucket;
    }

    public void OnCanvasChange(int width, int height) {
        mDateCenter.x = width / 2;
        mDateCenter.y = height - 30;
    }

    public void DrawDate(Canvas canvas) {
        BinaryPaint datePaint = mPaintBucket.getDatePaint();
        BinaryPaint dateInsetPaint = mPaintBucket.getDateInsetPaint();
        BinaryPaint dateTextPaint = mPaintBucket.getDateTextPaint();

        Rect box = new Rect((int) mDateCenter.x - 15,
                (int) mDateCenter.y - 10,
                (int) mDateCenter.x + 15,
                (int) mDateCenter.y + 10);
        canvas.drawRect(box, datePaint);
        box.left += 1;
        box.top += 1;
        box.bottom -= 1;
        box.right -= 1;
        canvas.drawRect(box, dateInsetPaint);


        canvas.drawText("29", mDateCenter.x, mDateCenter.y, dateTextPaint);

    }
}
