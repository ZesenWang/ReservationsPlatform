package com.example.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.main.MainActivity;
import com.example.utils.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangz on 2016/12/23.
 */

public class CheckIntentService extends IntentService {
    private static final String TAG = "CheckIntentService";
    private static final String UPDATE_UI_BROAD = "com.example.action.UPDATE_UI";

    public CheckIntentService(){
        // TODO: 2016/12/23 give a parameter
        super("HelloWorkerThread");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "service is running");

        final SharedPreferences preferences = getSharedPreferences("waitInfo", MODE_PRIVATE);
        SharedPreferences infoPreference = PreferenceManager.getDefaultSharedPreferences(CheckIntentService.this);
        //如果没有预约就直接返回
        if(!preferences.getBoolean("isReserveSucceed", false))
            return;
        //放入请求信息
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", infoPreference.getString("sId", ""));
            jsonObject.put("isCancel", preferences.getBoolean("isCancel", false));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONHelper helper = new JSONHelper(jsonObject);
        helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
            @Override
            public void onReceive(JSONObject result) {
                //没收到结果就返回
                if(result == null)
                    return;
                //收到结果就更新UI
                Intent intent = new Intent(UPDATE_UI_BROAD);
                try {
                    int queueNumber = result.getInt("queueNumber");
                    int waitTime = result.getInt("waitTime");
                    int peopleNumber = result.getInt("peopleNumber");
                    //排队号码是-1说明挂号取消了
                    if(queueNumber == -1){
                        //把所以数据恢复初始值
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isReserveSucceed",false);
                        editor.putBoolean("isCancel", false);
                        editor.putInt("queueNumber",-1);
                        editor.putInt("waitTime", -1);
                        editor.putInt("peopleNumber", -1);
                        editor.apply();
                        //告诉广播接收者要重置UI
                        intent.putExtra("msg","reset");
                    }
                    else {
                        //这个分支说明正常收到消息，应该更新UI
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("queueNumber", queueNumber);
                        editor.putInt("waitTime", waitTime);
                        editor.putInt("peopleNumber", peopleNumber);
                        editor.apply();
                        //告诉广播接收者更新UI
                        intent.putExtra("msg","update");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //发送广播让主线程更新UI
                sendBroadcast(intent);
            }
        });
        helper.interact(MainActivity.SERVER_URL + "/CheckServlet");
    }
}
