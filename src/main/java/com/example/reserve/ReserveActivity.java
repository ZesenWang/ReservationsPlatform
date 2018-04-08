package com.example.reserve;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.example.activity.R;
import com.example.reserve.fragment.ReserveFragment1;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangz on 2016/10/29.
 */

public class ReserveActivity extends AppCompatActivity{
    ActionBar actionBar;
    LinearLayout container;
    ReserveFragment1 fragment1;
    public JSONObject reservationInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        container = (LinearLayout)findViewById(R.id.container);

        fragment1 = new ReserveFragment1();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment1);
        transaction.addToBackStack(null);
        transaction.commit();

        reservationInfo = new JSONObject();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            reservationInfo.put("hospital",getIntent().getStringExtra("hospital"));
            reservationInfo.put("id", preferences.getString("sId", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            getFragmentManager().popBackStackImmediate();
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            }
        }
        //注意这里如果返回了true，fragment就不能自己处理了
        return super.onOptionsItemSelected(item);
    }
}
