package com.example.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.main.MainActivity;
import com.example.activity.R;
import com.example.fragment.SimpleDraweeViewFragment;

/**
 * Created by wangz on 2016/8/10.
 */
public class ImagePagerAdapter extends FragmentPagerAdapter {

    MainActivity context;
    private static String TAG = "ImageLoader";
    private static final int[] IMAGES = {
            R.drawable.screenshot1,
            R.drawable.screenshot2,
            R.drawable.screenshot3,
            R.drawable.screenshot4
    };

    public ImagePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = (MainActivity) context;
    }

    @Override
    public int getCount() {
        return IMAGES.length;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "getItem: position: "+position);
        // 使用简单的imageview
        //ImageFragment.setContext(context);
        //ImageFragment imageFragment = new ImageFragment();
        //context.imageHelper.bindBitmapFromResource(IMAGES[position], imageFragment.imageView, imageFragment.imageView.getWidth(), imageFragment.imageView.getHeight());
        //使用Fresco框架
        SimpleDraweeViewFragment fragment = new SimpleDraweeViewFragment();
        fragment.setImageRes(IMAGES[position]);

        return fragment;
    }
}
