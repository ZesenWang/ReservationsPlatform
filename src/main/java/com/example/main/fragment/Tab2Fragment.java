package com.example.main.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.main.MainActivity;
import com.example.adapter.ImagePagerAdapter;
import com.example.activity.R;

import java.util.Timer;

/**
 * Created by wangz on 2016/9/25.
 */
public class Tab2Fragment extends Fragment {
    private static final String TAG = "tab2fragment";
    boolean isLoop = true;
    int IMAGE_ITEM = 4;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentItem = pager.getCurrentItem() + 1;
            currentItem = currentItem % IMAGE_ITEM;
            pager.setCurrentItem(currentItem);
            if(isLoop)
                sendEmptyMessageDelayed(0x123,3000);
        }
    };
    ViewPager pager;
    Timer timer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_activity_tab2, container, false);

        MainActivity activity = (MainActivity)getActivity();

        pager = (ViewPager)view.findViewById(R.id.viewPager);
        pager.setAdapter(new ImagePagerAdapter( activity.getSupportFragmentManager(),getActivity()));
        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch: "+event.getAction());
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isLoop = false;
                        handler.removeMessages(0x123);
                        break;
                    case MotionEvent.ACTION_UP:
                        isLoop = true;
                        handler.sendEmptyMessageDelayed(0x123, 3000);
                        break;
                }
                return false;
            }
        });
        handler.sendEmptyMessageDelayed(0x123, 3000);

        setToolbar(view);
        return view;
    }

    private void setToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolBar);
        toolbar.setTitle("预约挂号平台");
        toolbar.setTitleTextColor(0xffffffff);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeMessages(0x123);
    }

}
