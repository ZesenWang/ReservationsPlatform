package com.example.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.main.MainActivity;
import com.example.utils.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangz on 2016/12/25.
 */

public class CheckBindingService extends Service {
    private static final String TAG = "CheckBindingService";
    MyBinder binder = new MyBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        checkout();
        return binder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkout();
        //getBaseContext();
        return super.onStartCommand(intent, flags, startId);
    }



    public interface UIOperations{
        void resetUI();
        void updateUI();
    }
    public class MyBinder extends Binder {
        UIOperations operations;
        public void checkoutInService(){
            checkout();
        }
        public void setUIOperations(UIOperations operations){
            this.operations = operations;
        }
    }
    public void checkout(){
        Log.i(TAG, "onStartCommand: service is running");
        final SharedPreferences preferences = getSharedPreferences("waitInfo", MODE_PRIVATE);
        SharedPreferences userInfoPreference = PreferenceManager.getDefaultSharedPreferences(this);

        if(!preferences.getBoolean("isReserveSucceed", false)) {
            Log.i(TAG, "checkout: No reservation Quit checkout");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", userInfoPreference.getString("sId", ""));
            jsonObject.put("isCancel", preferences.getBoolean("isCancel", false));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONHelper helper = new JSONHelper(jsonObject);
        helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
            @Override
            public void onReceive(JSONObject result) {
                if(result == null)
                    return;
                //Intent intent = new Intent("com.example.action.UPDATE_UI");
                try {
                    int queueNumber = result.getInt("queueNumber");
                    int waitTime = result.getInt("waitTime");
                    int peopleCount = result.getInt("peopleCount");
                    if(queueNumber == -1){
                        Log.i(TAG, "onReceive: queueNumber=-1");
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isReserveSucceed",false);
                        editor.putBoolean("isCancel", false);
                        editor.putInt("queueNumber",-1);
                        editor.putInt("waitTime", -1);
                        editor.putInt("peopleCount", -1);
                        editor.commit();
                        //使用intent发送广播，放弃的方法
                        //intent.putExtra("msg","reset");
                        //调用主线程实现的接口里的方法
                        binder.operations.resetUI();
                    }
                    else {
                        Log.i(TAG, "onReceive: queueNumber= 1");
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("queueNumber", queueNumber);
                        editor.putInt("waitTime", waitTime);
                        editor.putInt("peopleCount", peopleCount);
                        editor.commit();
                        //使用intent发送广播，放弃的方法
                        //intent.putExtra("msg","update");
                        //调用主线程实现的接口里的方法
                        binder.operations.updateUI();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //sendBroadcast(intent);
            }
        });
        helper.interact(MainActivity.SERVER_URL + "/CheckServlet");
    }
}
