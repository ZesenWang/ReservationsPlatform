package com.example.dentalhospital;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

/**
 * Created by wangz on 2016/10/23.
 */

public class HospitalDetailsActivity extends AppCompatActivity {
    TextView textView, textView9, textView10;
    MapView mapView;
    BaiduMap baiduMap;
    LatLng latLng;
    ActionBar actionBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.hospital_details);

        textView = (TextView)findViewById(R.id.textView);
        textView9 = (TextView)findViewById(R.id.textView9);
        textView10 = (TextView)findViewById(R.id.textView10);
        mapView = (MapView)findViewById(R.id.mapView);
        baiduMap = mapView.getMap();

        Intent intent = getIntent();
        textView.setText(intent.getStringExtra("name"));
        textView9.setText(intent.getStringExtra("address"));
        textView10.setText(intent.getStringExtra("phoneNum"));

        latLng = intent.getParcelableExtra("location");

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        textView10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhoneNumber(textView10.getText().toString());
            }
        });
    }
    public void onReserve(View view){
        Intent intent = new Intent(getApplicationContext(), ReserveActivity.class);
        intent.putExtra("hospital",getIntent().getStringExtra("name"));
        startActivity(intent);
    }
    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
        MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);

        baiduMap.setMapStatus(status);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.googlemapmarker);
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);
        baiduMap.addOverlay(option);
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
