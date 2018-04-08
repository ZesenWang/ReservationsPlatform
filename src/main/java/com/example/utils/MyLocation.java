package com.example.utils;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.main.fragment.Tab1Fragment;

/**
 * Created by wangz on 2016/11/18.
 */

public class MyLocation {
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    public BDLocation location;
    public Tab1Fragment tab1Fragment;

    public MyLocation(Context context, Tab1Fragment tab1Fragment){
        this.tab1Fragment = tab1Fragment;
        mLocationClient = new LocationClient(context);     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocationClient.setLocOption(option);
    }
//    public double distance(double latitude, double longitude, LatLng destination){
//        double sum = Math.pow(latitude - destination.latitude, 2) + Math.pow(longitude - destination.longitude, 2);
//        return Math.pow(sum, 0.5);
//    }
    class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            tab1Fragment.setBDLocation(bdLocation);
            location = bdLocation;
            mLocationClient.stop();
        }
    }
}
