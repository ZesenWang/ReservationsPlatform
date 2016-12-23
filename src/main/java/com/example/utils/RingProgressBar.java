package com.example.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wangz on 2016/11/19.
 */

public class RingProgressBar extends View {
    Paint paint = new Paint();
    GradientColorMaker maker = new GradientColorMaker();
    public RingProgressBar(Context context) {
        super(context);
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xff00fa44);
        paint.setStrokeWidth(24);
        paint.setAntiAlias(true);

        RectF rect = new RectF(12, 12, getWidth() - 12, getHeight() - 12);
        maker.setStartColor(0xff00fa99);
        maker.setEndColor(0xff00fa44);
        maker.setRepeatCount(50);
        int startAngle = 0;
        for(int i = 0;i < 50; i++){
            paint.setColor(maker.get(i));
            canvas.drawArc(rect, startAngle, 3, false, paint);
            startAngle += 3;
        }
    }
}
