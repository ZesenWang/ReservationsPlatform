package com.example.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dentalhospital.MainActivity;
import com.example.dentalhospital.R;
import com.example.fragment.ImageFragment;

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
        ImageFragment.setContext(context);
        ImageFragment imageFragment = new ImageFragment();
        context.imageHelper.bindBitmapFromResource(IMAGES[position], imageFragment.imageView, imageFragment.imageView.getWidth(), imageFragment.imageView.getHeight());
        return imageFragment;
    }
}
