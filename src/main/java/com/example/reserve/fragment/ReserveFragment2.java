package com.example.reserve.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.main.MainActivity;
import com.example.activity.R;
import com.example.reserve.ReserveActivity;
import com.example.utils.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangz on 2016/10/29.
 */

public class ReserveFragment2 extends Fragment implements AdapterView.OnItemClickListener{
    TextView textView, hospitalText, doctorText, feeText;
    ListView listView;
    ArrayAdapter adapter;
    ProgressDialog dialog;
    View customToast;
    View parentView;
    JSONHelper helper;
    String[] departmentArray, doctorArray;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //这时才真正的开始与服务器通信
            helper.interact(MainActivity.SERVER_URL + "/ReserveServlet");
        }
    };
    ReserveActivity activity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_reserve_activity_fragment1, container, false);
        textView = (TextView)parentView.findViewById(R.id.textView16);
        listView = (ListView)parentView.findViewById(R.id.listView);
        textView.setText("请选择医生");

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.doctor_names, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        customToast = getActivity().getLayoutInflater().inflate(R.layout.toast_reserve_activity_fragment3_customtoast, container, false);
        departmentArray = getResources().getStringArray(R.array.department);
        doctorArray = getResources().getStringArray(R.array.doctor_names);
        return parentView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        activity = (ReserveActivity)getActivity();
        String doctors[] = getActivity().getResources().getStringArray(R.array.doctor_names);
        if(position == 0){
            try {
                activity.reservationInfo.put("reservationType",0);
                activity.reservationInfo.put("doctor","普通号");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            View view = getActivity().getLayoutInflater().inflate(R.layout.window_reserve_activity_fragment3_pay, null);
            Button button = (Button)view.findViewById(R.id.button);
            button.setOnClickListener(new ReservingListener());
            try {
                ((TextView)view.findViewById(R.id.textView18)).setText("医    院："+activity.reservationInfo.getString("hospital"));
                ((TextView)view.findViewById(R.id.textView19)).setText("科    室："+activity.reservationInfo.getString("department"));
                ((TextView)view.findViewById(R.id.textView20)).setText("医    生："+activity.reservationInfo.getString("doctor"));
                ((TextView)view.findViewById(R.id.textView21)).setText("挂号费：10元");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setAnimationStyle(R.style.reserveactivity_fragment3_popupwindow);
            popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        }else {
            try {
                activity.reservationInfo.put("reservationType",1);
                activity.reservationInfo.put("doctor",doctors[position]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, new ReserveFragment3());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public class ReservingListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            dialog = ProgressDialog.show(getActivity(), null, "与服务器通信中", true);
            handler.sendEmptyMessageDelayed(0x123, 2000);

            helper = new JSONHelper(activity.reservationInfo);
            helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
                @Override
                public void onReceive(JSONObject result) {
                    //关闭对话框不算更新UI
                    dialog.dismiss();
                    try {
                        if(result == null || !result.getBoolean("isSucceed")){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "当前不是挂号时间！", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent backToMain = new Intent(getActivity(),MainActivity.class);
                            startActivity(backToMain);
                            return;
                        }
                        //继续执行这里代码表示挂号成功
                        int waitTime = result.getInt("waitTime");
                        int peopleCount = result.getInt("peopleCount");

                        SharedPreferences preferences = activity.getSharedPreferences("waitInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("waitTime",waitTime);
                        editor.putInt("peopleCount",peopleCount);
                        editor.putString("doctor",activity.reservationInfo.getString("doctor"));
                        editor.putBoolean("isReserveSucceed", true);
                        editor.putBoolean("isCancel",false);
                        editor.apply();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = new Toast(getActivity());
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0,0);
                                toast.setView(customToast);
                                toast.show();
                            }
                        });

                        Intent backToMain = new Intent(getActivity(),MainActivity.class);
                        startActivity(backToMain);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}