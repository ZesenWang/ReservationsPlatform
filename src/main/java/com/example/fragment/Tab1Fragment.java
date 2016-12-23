package com.example.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.dentalhospital.HospitalDetailsActivity;
import com.example.dentalhospital.R;
import com.example.utils.CustomAdapter;
import com.example.utils.FunctionGridViewAdapter;
import com.example.utils.MyLocation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangz on 2016/9/25.
 */
public class Tab1Fragment extends Fragment
        implements AdapterView.OnItemClickListener{

    GridView gridView;
    GridView gridView2;
    SimpleAdapter simpleAdapter;
    CustomAdapter customAdapter;
    public static ArrayList<PoiInfo> poiList;
    TextView refresh;
    BDLocation location;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1, container, false);
        gridView = (GridView)view.findViewById(R.id.gridView);
        simpleAdapter = new FunctionGridViewAdapter(getActivity()).getAdapter();
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(this);

        refresh = (TextView)view.findViewById(R.id.refresh);
        gridView2 = (GridView)view.findViewById(R.id.gridView2);
        gridView2.setEmptyView(refresh);
        gridView2.setAdapter(customAdapter);
        gridView2.setOnItemClickListener(gridView2Listener);

        return view;
    }


    AdapterView.OnItemClickListener gridView2Listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.i("info",""+position+" "+ id);
            if(adapterView == gridView2){
                Intent intent = new Intent(getActivity(), HospitalDetailsActivity.class);
                PoiInfo info = poiList.get(position);
                intent.putExtra("phoneNum", info.phoneNum)
                        .putExtra("address", info.address)
                        .putExtra("location",info.location)
                        .putExtra("name",info.name);
                startActivity(intent);
            }
        }
    };
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.i("info",""+position+" "+ id);
        if(position == 0){
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            PrintFragment fragment = new PrintFragment();
            transaction.replace(R.id.container, fragment).addToBackStack(null).commit();
        }
    }

    public  void setPoiInfoList(ArrayList<PoiInfo> poi){
        poiList = poi;
        check();
    }
    public void setBDLocation(BDLocation location){
        this.location = location;
        check();
    }
    public void check(){
        if(poiList != null && location != null){
            customAdapter = new CustomAdapter(getActivity(), poiList, location);
            gridView2.setAdapter(customAdapter);
        }
    }
}