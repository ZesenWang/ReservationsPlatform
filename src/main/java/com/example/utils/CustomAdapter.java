package com.example.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.dentalhospital.MainActivity;
import com.example.dentalhospital.R;
import com.example.utils.MyLocation;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private final static String TAG = "DentalHospital";
    int imageId[] = {R.drawable.h1,R.drawable.h2,R.drawable.h3,R.drawable.h4,R.drawable.h5,
            R.drawable.h6};
    float ratings[] = {5,4.5f,4f,3.5f,4};
    MainActivity context;
    ImageView imageView;
    TextView name, distance;
    ArrayList<PoiInfo> poiList;
    BDLocation location;
    RatingBar ratingBar;
    PoiSearch poiSearch;
    int currentPageNum;

    public CustomAdapter(Context context, ArrayList<PoiInfo> poiList, BDLocation location){
        this.poiList = poiList;
        this.context = (MainActivity)context;
        this.location = location;
        poiSearch = PoiSearch.newInstance();
    }
    @Override
    public int getCount() {
        return poiList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.i(TAG, "position: " + i);
        //当滑动到最后一个时，就重新发起搜索，请求第二页
        if(i == (poiList.size()-1)){
            poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").keyword("医院").pageCapacity(6).pageNum(++currentPageNum));
            poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
                @Override
                public void onGetPoiResult(PoiResult poiResult) {
                    if(SearchResult.ERRORNO.NO_ERROR == poiResult.error) {
                        //把搜索的结果添加到原来的数组里
                        ArrayList<PoiInfo> poiInfo = (ArrayList<PoiInfo>) poiResult.getAllPoi();
                        poiList.addAll(poiInfo);
                        notifyDataSetChanged();
                    }else {
                        Log.i(TAG, "onGetPoiResult: Search Next Page Fail, "+poiResult.error);
                    }
                }

                @Override
                public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

                }

                @Override
                public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

                }
            });
        }
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.tab1_gridview2_item, viewGroup, false);
        }
        imageView = (ImageView) view.findViewById(R.id.imageView2);
        name = (TextView)view.findViewById(R.id.textView2);
        distance = (TextView)view.findViewById(R.id.textView3);
        ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);

        PoiInfo info = poiList.get(i);
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng destination = info.location;
//        Log.i("location", ""+currentLatitude);
//        Log.i("location", ""+currentLongitude);
//        Log.i("location", ""+destination.latitude);
//        Log.i("location", ""+destination.longitude);
        int distanceFromDest = (int)DistanceUtil.getDistance(current, destination);
        distance.setText("距离我的位置："+distanceFromDest+"米");
        //评分是假的
        int fakeRatingIndex = i % 5;
        ratingBar.setRating(ratings[fakeRatingIndex]);
        //没有准备很多图片，所以只能循环这6张图
        int fakeImageIndex = i % 6;
        context.imageHelper.bindBitmapFromResource(imageId[fakeImageIndex], imageView, imageView.getWidth(), imageView.getHeight());

        name.setText(info.name);
        return view;
    }
}