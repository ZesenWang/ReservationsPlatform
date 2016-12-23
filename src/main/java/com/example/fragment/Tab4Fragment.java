package com.example.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.dentalhospital.R;
import com.example.service.CheckService;
import com.example.utils.RingProgressBar;

/**
 * Created by wangz on 2016/9/25.
 */
public class Tab4Fragment extends Fragment {

    RingProgressBar ringProgressBar;
    public TextView waitTime, peopleNumber, queueNumber,reservationType;
    Button cancelReservation;
    ProgressDialog dialog;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dialog.dismiss();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab4, container, false);
        cancelReservation = (Button)view.findViewById(R.id.cancel);
        cancelReservation.setOnClickListener(listener);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.mainactivity_fragment4_ringbar);
        ringProgressBar = (RingProgressBar)view.findViewById(R.id.ringBar);
        ringProgressBar.setAnimation(animation);

        queueNumber = (TextView)view.findViewById(R.id.queueNumber);
        waitTime = (TextView)view.findViewById(R.id.waitTime);
        peopleNumber = (TextView)view.findViewById(R.id.peopleNumber);
        reservationType = (TextView)view.findViewById(R.id.reservationType);
        return view;
    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog = ProgressDialog.show(getActivity(),null, "取消中。。。",true);

            SharedPreferences.Editor editor = getActivity().getSharedPreferences("waitInfo", Context.MODE_PRIVATE).edit();
            editor.putBoolean("isCancel",true).apply();

            Intent intent = new Intent(getActivity(), CheckService.class);
            getActivity().startService(intent);

            handler.sendEmptyMessageDelayed(0x123, 1500);
        }
    };
}
