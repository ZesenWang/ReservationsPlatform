package com.example.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dentalhospital.MainActivity;
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
        super(null);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "service is running");
        final SharedPreferences preferences = getSharedPreferences("waitInfo", MODE_PRIVATE);
        SharedPreferences infoPreference = PreferenceManager.getDefaultSharedPreferences(CheckIntentService.this);

        if(!preferences.getBoolean("isReserveSucceed", false))
            return;

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
                if(result == null)
                    return;
                Intent intent = new Intent(UPDATE_UI_BROAD);
                try {
                    int queueNumber = result.getInt("queueNumber");
                    int waitTime = result.getInt("waitTime");
                    int peopleNumber = result.getInt("peopleNumber");
                    if(queueNumber == -1){
                        Toast.makeText(CheckIntentService.this, "取消挂号成功或者当前号码已被处理", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isReserveSucceed",false);
                        editor.putBoolean("isCancel", false);
                        editor.putInt("queueNumber",-1);
                        editor.putInt("waitTime", -1);
                        editor.putInt("peopleNumber", -1);
                        editor.apply();

                        intent.putExtra("msg","reset");
                    }
                    else {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("queueNumber", queueNumber);
                        editor.putInt("waitTime", waitTime);
                        editor.putInt("peopleNumber", peopleNumber);
                        editor.apply();

                        intent.putExtra("msg","update");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendBroadcast(intent);
            }
        });
        helper.interact(MainActivity.SERVER_URL + "/CheckServlet");
    }
}
