package com.example.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.dentalhospital.MainActivity;
import com.example.dentalhospital.R;

public class ActivityReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivityReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: i am working");
//        String []doctor = getResources().getStringArray(R.array.doctor_names);
//        String msg = intent.getStringExtra("msg");
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//
//        int waitTime = preferences.getInt("waitTime",-1);
//        int peopleNumber = preferences.getInt("peopleNumber", -1);
//        int queueNumber = preferences.getInt("queueNumber", -1);
//
//        if(msg.equals("reset")){
//            tab4Fragment.waitTime.setText("预计排队时间\\n\\n分钟");
//            tab4Fragment.peopleNumber.setText("");
//            tab4Fragment.queueNumber.setText("你的排队号码\n\n");
//            tab4Fragment.reservationType.setText("你的挂号类型\n\n");
//        }else if(msg.equals("update")){
//            tab4Fragment.waitTime.setText("预计排队时间\\n\\n"+waitTime+"分钟");
//            tab4Fragment.peopleNumber.setText(""+peopleNumber);
//            tab4Fragment.queueNumber.setText("你的排队号码\n\n"+queueNumber);
//            tab4Fragment.reservationType.setText("你的挂号类型\n\n"+doctor[preferences.getInt("doctor",-1)]);
//        }
    }
}