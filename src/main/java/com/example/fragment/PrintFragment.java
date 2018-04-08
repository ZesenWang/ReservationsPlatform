package com.example.fragment;


import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.main.MainActivity;
import com.example.utils.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangz on 2016/12/2.
 */

public class PrintFragment extends Fragment {
    ActionBar actionBar;
    ProgressDialog dialog;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dialog.dismiss();
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = ProgressDialog.show(getActivity(),null, "打印中......",true);
        actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final MainActivity activity = (MainActivity)getActivity();
        JSONObject jsonObject = new JSONObject();
        SharedPreferences preferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        try {
            jsonObject.put("id", preferences.getString("sId", null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONHelper helper = new JSONHelper(jsonObject);
        helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
            @Override
            public void onReceive(JSONObject result) {
                try {
                    if(result == null || !result.getBoolean("isSucceed")){
                        Toast.makeText(activity, "当前不是挂号时间！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int waitTime = result.getInt("waitTime");
                    int peopleCount = result.getInt("peopleCount");
                    int queueNumber = result.getInt("queueNumber");
                    SharedPreferences preferences = activity.getSharedPreferences("waitInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("waitTime",waitTime);
                    editor.putInt("peopleCount",peopleCount);
                    editor.putInt("queueNumber",queueNumber);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        helper.interact(null);

        handler.sendEmptyMessageDelayed(0x123, 2000);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("号码纸已打印，请到相应的取票机下取号");
        textView.setTextSize(20.0f);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}
