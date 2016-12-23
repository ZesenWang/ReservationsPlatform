package com.example.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;

import com.example.dentalhospital.R;

/**
 * Created by wangz on 2016/8/12.
 */
public class TabButton extends Button {

    public Paint paint;
    public Bitmap bitmap;
//    private  float mTextSize = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_SP,20,getResources()
//                    .getDisplayMetrics()
//    );
    public String TAG = "附近";

    public TabButton(Context context){
        super(context);
        init();
    }
    public TabButton(Context context, AttributeSet attr){
        super(context,attr);
        init();
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(35);
        paint.setColor(0xff3385ff);
        paint.setStrokeWidth(3);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * 0.25),
//                (int)(bitmap.getHeight() * 0.25),false);
        //draw bitmap
        int left = getWidth()/2 - bitmap.getWidth()/2;
        int top = getHeight()/2 - (int)(bitmap.getHeight() * 0.6);
        canvas.drawBitmap(bitmap, left, top, null);
        //draw text
        float x = getWidth()/2 - paint.measureText(TAG)/2;
        canvas.drawText(TAG, x, getHeight() * 8/9, paint);
    }
}
