package com.example.hospitaldetail;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.example.activity.R;
import com.example.activity.databinding.ActivityHospitalDetailsBinding;
import com.example.reserve.ReserveActivity;


/**
 * Created by wangz on 2016/10/23.
 */

public class HospitalDetailsActivity extends AppCompatActivity implements HDView {

    private static final String TAG = "ReservationApp";

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mLatLng;
    private ActionBar mActionBar;
    private HDViewModel mModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        // 初始化ViewModel
        mModel = new HDViewModel(getIntent().getExtras(), this);
        ActivityHospitalDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_hospital_details);
        binding.setI(mModel);

        // 初始化百度地图
        mMapView = binding.mapView;
        mBaiduMap = mMapView.getMap();

        // 初始化ActionBar
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        Intent intent = getIntent();
        mLatLng = intent.getParcelableExtra("location");

        //todo consider moving these codes into onCreateView();
        //todo Maybe BaiduMap is not ready
        MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(mLatLng, 18);
        mBaiduMap.setMapStatus(status);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.googlemapmarker);
        OverlayOptions option = new MarkerOptions()
                .position(mLatLng)
                .icon(bitmap);
        mBaiduMap.addOverlay(option);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void startBaiduMap() {
        Log.i(TAG, "start Baidu Map App");
        BDLocation myLocation = getIntent().getParcelableExtra("myLocation");
        LatLng location = getIntent().getParcelableExtra("location");

        RouteParaOption para = new RouteParaOption()
                .startPoint(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                .endPoint(location)
                .busStrategyType(RouteParaOption.EBusStrategyType.bus_recommend_way);
        try {
            BaiduMapRoutePlan.openBaiduMapTransitRoute(para, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //结束调启功能时调用finish方法以释放相关资源
        BaiduMapRoutePlan.finish(this);
    }

    public void goToReserveActivity() {
        Intent intent = new Intent(this, ReserveActivity.class);
        intent.putExtra("hospital", getIntent().getStringExtra("name"));
        startActivity(intent);
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
