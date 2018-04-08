package com.example.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by wangz on 2017/10/15.
 */

public class SimpleDraweeViewFragment extends Fragment {

    SimpleDraweeView mSimpleDraweeView;
    int mImageId;

    public void setImageRes(int imageRes){
        mImageId = imageRes;

        if(mSimpleDraweeView != null)
            mSimpleDraweeView.setImageResource(imageRes);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSimpleDraweeView = new SimpleDraweeView(getActivity());

        GenericDraweeHierarchy hierarchy = mSimpleDraweeView.getHierarchy();
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);

        if (mImageId != 0) {
            mSimpleDraweeView.setImageResource(mImageId);
        }
        return mSimpleDraweeView;
    }
}
