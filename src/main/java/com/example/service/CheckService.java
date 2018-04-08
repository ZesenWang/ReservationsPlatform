package com.example.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.main.MainActivity;
import com.example.utils.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckService extends Service {
    private static final String TAG = "CheckService";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: service is running");
        final SharedPreferences preferences = getSharedPreferences("waitInfo", MODE_PRIVATE);

        if(!preferences.getBoolean("isReserveSucceed", false))
            return super.onStartCommand(intent, flags, startId);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", preferences.getString("sId", null));
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
                Intent intent = new Intent("com.example.action.UPDATE_UI");
                try {
                    int queueNumber = result.getInt("queueNumber");
                    int waitTime = result.getInt("waitTime");
                    int peopleNumber = result.getInt("peopleNumber");
                    if(queueNumber == -1){
                        Toast.makeText(CheckService.this, "取消挂号成功或者当前号码已被处理", Toast.LENGTH_SHORT).show();
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
        return super.onStartCommand(intent, flags, startId);
    }
}
