package com.example.reserve.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

public class ReserveFragment3 extends Fragment implements View.OnClickListener{
    Button button9, button10;
    Drawable drawable,drawable2,drawable3,drawable4;
    LinearLayout linearLayout;
    TextView profile, schedule;
    FrameLayout.LayoutParams params;
    Button button;
    View parentView;
    ProgressDialog dialog;
    View customToast;
    String[] departmentArray, doctorArray;
    JSONHelper helper;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            helper.interact(MainActivity.SERVER_URL + "/ReserveServlet");
        }
    };
    ReserveActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        customToast = getActivity().getLayoutInflater().inflate(R.layout.toast_reserve_activity_fragment3_customtoast, null);
        departmentArray = getResources().getStringArray(R.array.department);
        doctorArray = getResources().getStringArray(R.array.doctor_names);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_reserve_activity_fragment3, container, false);

        linearLayout = (LinearLayout)parentView.findViewById(R.id.dynamicContainer);
        button9 = (Button)parentView.findViewById(R.id.button9);
        button10 = (Button)parentView.findViewById(R.id.button10);

        drawable = getResources().getDrawable(R.drawable.left_button_checked);
        drawable2 = getResources().getDrawable(R.drawable.left_button_unchecked);
        drawable3 = getResources().getDrawable(R.drawable.right_button_checked);
        drawable4 = getResources().getDrawable(R.drawable.right_button_unchecked);

        button9.setOnClickListener(this);
        button10.setOnClickListener(this);

        profile = new TextView(getActivity());
        profile.setText(R.string.doctor_chenyue);
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        schedule = new TextView(getActivity());
        schedule.setText(R.string.doctor_chenyue_schedule);
        linearLayout.addView(schedule, params);
        return parentView;
    }

    public void onClick(View view){
        if(view.getId() == R.id.button9){
            button9.setBackground(drawable);
            button9.setTextColor(0xffffffff);
            button10.setBackground(drawable4);
            button10.setTextColor(0xff000000);

            linearLayout.removeAllViews();
            linearLayout.addView(schedule, params);
        }
        else if(view.getId() == R.id.button10){
            button9.setBackground(drawable2);
            button9.setTextColor(0xff000000);
            button10.setBackground(drawable3);
            button10.setTextColor(0xffffffff);

            linearLayout.removeAllViews();
            linearLayout.addView(profile, params);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reserve_activity_fragment3_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        activity = (ReserveActivity)getActivity();
        if(item.getItemId() == R.id.next){
            //生成付款的view
            View view = getActivity().getLayoutInflater().inflate(R.layout.window_reserve_activity_fragment3_pay, null);
            button = (Button)view.findViewById(R.id.button);
            button.setOnClickListener(new ReservingListener());
            try {
                ((TextView)view.findViewById(R.id.textView18)).setText("医    院："+activity.reservationInfo.getString("hospital"));
                ((TextView)view.findViewById(R.id.textView19)).setText("科    室："+activity.reservationInfo.getString("department"));
                ((TextView)view.findViewById(R.id.textView20)).setText("医    生："+activity.reservationInfo.getString("doctor"));
                ((TextView)view.findViewById(R.id.textView21)).setText("挂号费：50元");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setAnimationStyle(R.style.reserveactivity_fragment3_popupwindow);
            popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public class ReservingListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            dialog = ProgressDialog.show(getActivity(), null, "与服务器通信中", true);
            handler.sendEmptyMessageDelayed(0x123, 2000);
            helper = new JSONHelper(activity.reservationInfo);
            helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
                @Override
                public void onReceive(JSONObject result) {
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
                        //// TODO: 2016/12/23
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
