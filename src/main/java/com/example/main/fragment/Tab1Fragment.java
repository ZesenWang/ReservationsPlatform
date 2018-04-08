package com.example.main.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.search.core.PoiInfo;
import com.example.fragment.PrintFragment;
import com.example.hospitaldetail.HospitalDetailsActivity;
import com.example.main.MainActivity;
import com.example.activity.R;
import com.example.adapter.CustomAdapter;
import com.example.adapter.FunctionGridViewAdapter;

import java.util.ArrayList;

/**
 * Created by wangz on 2016/9/25.
 */
public class Tab1Fragment extends Fragment implements AdapterView.OnItemClickListener {

    private GridView gridView;
    private GridView gridView2;
    private SimpleAdapter simpleAdapter;
    private CustomAdapter customAdapter;
    public static ArrayList<PoiInfo> poiList;
    private TextView refresh;
    private BDLocation location;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_activity_tab1, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        simpleAdapter = new FunctionGridViewAdapter(getActivity()).getAdapter();
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(this);

        refresh = (TextView) view.findViewById(R.id.refresh);
        gridView2 = (GridView) view.findViewById(R.id.gridView2);
        gridView2.setEmptyView(refresh);
        gridView2.setAdapter(customAdapter);
        gridView2.setOnItemClickListener(gridView2Listener);

        setToolbar(view);

        return view;
    }

    private void setToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolBar);
        toolbar.setTitle("预约挂号平台");
        toolbar.setTitleTextColor(0xffffffff);
    }

    AdapterView.OnItemClickListener gridView2Listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.i("info", "" + position + " " + id);
            if (adapterView == gridView2) {
                Intent intent = new Intent(getActivity(), HospitalDetailsActivity.class);
                PoiInfo info = poiList.get(position);
                intent.putExtra("phoneNum", info.phoneNum)
                        .putExtra("address", info.address)
                        .putExtra("location", info.location)
                        .putExtra("name", info.name)
                        .putExtra("myLocation", ((MainActivity) getActivity()).myLocation.location);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.i("info", "" + position + " " + id);
        if (position == 0) {
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            PrintFragment fragment = new PrintFragment();
            transaction.replace(R.id.container, fragment).addToBackStack(null).commit();
        }
    }

    public void setPoiInfoList(ArrayList<PoiInfo> poi) {
        poiList = poi;
        check();
    }

    public void setBDLocation(BDLocation location) {
        this.location = location;
        check();
    }

    public void check() {
        if (poiList != null && location != null) {
            customAdapter = new CustomAdapter(getActivity(), poiList, location);
            gridView2.setAdapter(customAdapter);
        }
    }
}