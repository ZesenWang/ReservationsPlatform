package com.example.utils;

import android.app.Activity;
import android.widget.ImageView;

/**
 * Created by wangz on 2016/12/11.
 */

public class ImageHelper {
    private ImageLoader mImageLoader;

    public ImageHelper(Activity activity){
        mImageLoader = ImageLoader.build(activity);
    }
    public void bindBitmapFromResource(int resId, ImageView imageView, int reqWidth, int reqHeight){
        Integer tag = (Integer)imageView.getTag();
        if((tag == null)||(resId != tag.intValue())){
            imageView.setImageBitmap(null);
            imageView.setBackgroundColor(0x99ff0000);
        }
        imageView.setTag(resId);
        mImageLoader.bindBitmap(resId,imageView, imageView.getWidth(), imageView.getHeight());
    }
    public void bindBitmapFromUrl(String uri, ImageView imageView){
        final String tag = (String)imageView.getTag();
        if(!uri.equals(tag)){
            //这一步是判断两种情况
            //1.如果这个View是新inflate出来的，就会变成红色
            //2.如果这个view是被回收的，也会变红，并清除上一个view的图片
            imageView.setImageBitmap(null);
            imageView.setBackgroundColor(0x99ff0000);
        }
        imageView.setTag(uri);
        mImageLoader.bindBitmap(uri, imageView, imageView.getWidth(), imageView.getHeight());
    }
}
