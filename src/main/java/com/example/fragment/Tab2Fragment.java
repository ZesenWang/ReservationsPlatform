package com.example.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.dentalhospital.MainActivity;
import com.example.utils.ImagePagerAdapter;
import com.example.dentalhospital.R;

import java.util.Timer;
import java.util.TimerTask;

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
        View view = inflater.inflate(R.layout.tab2, container, false);

        pager = (ViewPager)view.findViewById(R.id.viewPager);
        MainActivity activity = (MainActivity)getActivity();
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
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeMessages(0x123);
    }
}
