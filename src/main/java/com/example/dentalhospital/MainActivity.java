package com.example.dentalhospital;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.example.fragment.Tab1Fragment;
import com.example.fragment.Tab2Fragment;
import com.example.fragment.Tab3Fragment;
import com.example.fragment.Tab4Fragment;
import com.example.service.CheckBindingService;
import com.example.service.CheckIntentService;
import com.example.service.CheckService;
import com.example.utils.ImageHelper;
import com.example.utils.MyLocation;
import com.example.utils.TabButton;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private static final String TAG = "mainactivity";
    public static String SERVER_URL = "";
    TabButton button,button2,button3,button4;
    Tab1Fragment tab1Fragment;
    Tab2Fragment tab2Fragment;
    Tab3Fragment tab3Fragment;
    Tab4Fragment tab4Fragment;
    FragmentManager fragmentManager;
    Bitmap tabBitmap[];
    PoiSearch poiSearch;
    MapView mapView;
    boolean isSignIn;
    SharedPreferences preferences;
    public ImageHelper imageHelper;
    Intent intent;
    CheckBindingService.MyBinder binder;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            //startService(intent);
            Log.i(TAG, "handleMessage: ");
            //bindService(intent, conn, Service.BIND_AUTO_CREATE);
            binder.checkoutInService();
            handler.sendEmptyMessageDelayed(0x123, 3000);
        }
    };
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (CheckBindingService.MyBinder)service;
            binder.setUIOperations(operations);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    CheckBindingService.UIOperations operations = new CheckBindingService.UIOperations() {
        @Override
        public void resetUI() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "取消挂号成功或者当前号码已被处理", Toast.LENGTH_SHORT).show();
                    //如果tab4还没有被用户点击过，也就是控件目前都为null，就不操作UI
                    if(tab4Fragment.waitTime == null)
                        return;
                    tab4Fragment.waitTime.setText("预计排队时间\n\n分钟");
                    tab4Fragment.peopleNumber.setText("");
                    tab4Fragment.queueNumber.setText("你的排队号码\n\n");
                    tab4Fragment.reservationType.setText("你的挂号类型\n\n");
                }
            });
        }

        @Override
        public void updateUI() {
            //如果tab4还没有被用户点击过，也就是控件目前都为null，就不操作UI
            if(tab4Fragment.waitTime == null)
                return;

            //更新UI, 好像每次启动新线程之后，即使线程结束了，代码还是跑在新线程
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //String []doctor = getResources().getStringArray(R.array.doctor_names);
                    //String msg = intent.getStringExtra("msg");
                    SharedPreferences preferences = getSharedPreferences("waitInfo",MODE_PRIVATE);
                    //从共享首选项里取出要显示的数据
                    int waitTime = preferences.getInt("waitTime",-1);
                    int peopleNumber = preferences.getInt("peopleNumber", -1);
                    int queueNumber = preferences.getInt("queueNumber", -1);
                    String doctor = preferences.getString("doctor", "");

                    tab4Fragment.waitTime.setText("预计排队时间\n\n"+waitTime+"分钟");
                    tab4Fragment.peopleNumber.setText(""+peopleNumber);
                    tab4Fragment.queueNumber.setText("你的排队号码\n\n"+queueNumber);
                    tab4Fragment.reservationType.setText("你的挂号类型\n\n"+doctor);
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initialize();

        tab1Fragment = new Tab1Fragment();
        tab2Fragment = new Tab2Fragment();
        tab3Fragment = new Tab3Fragment();
        tab4Fragment = new Tab4Fragment();
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, tab1Fragment).commit();

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        isSignIn = preferences.getBoolean("isSignIn",false);
        if(!isSignIn){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
        //当用户第二次打开app时(第一次打开时这里返回“”，因为还没有设置过)，
        // 取出SERVER_URL的值，在注册、挂号、检查时会使用
        SERVER_URL = preferences.getString("serverURL", "");
        //获取位置信息和附近的医院信息
        requestPOI();
        new MyLocation(this, tab1Fragment);
        //启动一个定时执行的service，用于检查挂号信息,因为广播接收者不能写成内部类，所以这个方法不行
        //intent = new Intent(this, CheckIntentService.class);
        //pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        //manager = (AlarmManager)getSystemService(Service.ALARM_SERVICE);
        //handler.sendEmptyMessageDelayed(0x123, 5000);
        //启动一个bound类型的service，可以与activity通信
        intent = new Intent(MainActivity.this, CheckBindingService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
        handler.sendEmptyMessageDelayed(0x123, 3000);
    }
    public void requestPOI(){
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiListener);
        poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").pageCapacity(60).keyword("医院"));
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // TODO: 2016/12/17 如果System.currentTimeMillis()不对，试试SystemClock.elapsedRealtime()
        //manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), 1000, pendingIntent);
        //getActionBar().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //manager.cancel(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        handler.removeMessages(0x123);
        unbindService(conn);
    }

    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {

            ArrayList<PoiInfo> list = (ArrayList<PoiInfo>) poiResult.getAllPoi();

            Log.i("info",poiResult.error + "");
            if(poiResult.error == SearchResult.ERRORNO.PERMISSION_UNFINISHED){
                poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").pageCapacity(60).keyword("医院"));
            }else if(poiResult.error == SearchResult.ERRORNO.NO_ERROR){
                tab1Fragment.setPoiInfoList(list);
                Log.i(TAG, "onGetPoiResult: listSize: "+list.size());
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }


    };
    @Override
    public void onClick(View view) {

        button.bitmap = tabBitmap[1];
        button2.bitmap = tabBitmap[3];
        button3.bitmap = tabBitmap[5];
        button4.bitmap = tabBitmap[7];

        button.paint.setColor(0xff757575);
        button2.paint.setColor(0xff757575);
        button3.paint.setColor(0xff757575);
        button4.paint.setColor(0xff757575);

        switch (view.getId()){
            case R.id.button:
                button.bitmap = tabBitmap[0];
                replaceFragment(tab1Fragment);
                button.paint.setColor(0xff3385ff);
                break;
            case R.id.button2:
                button2.bitmap = tabBitmap[2];
                replaceFragment(tab2Fragment);
                button2.paint.setColor(0xff3385ff);
                break;
            case R.id.button3:
                button3.bitmap = tabBitmap[4];
                replaceFragment(tab3Fragment);
                button3.paint.setColor(0xff3385ff);
                break;
            case R.id.button4:
                button4.bitmap = tabBitmap[6];
                replaceFragment(tab4Fragment);
                button4.paint.setColor(0xff3385ff);
                break;
        }
        button.invalidate();
        button2.invalidate();
        button3.invalidate();
        button4.invalidate();
    }
    private void replaceFragment(Fragment fragment){
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            getFragmentManager().popBackStackImmediate();
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return super.onOptionsItemSelected(item);
    }

    public void initialize(){
        mapView = new MapView(this);
        mapView.onCreate(this, null);

        button = (TabButton)findViewById(R.id.button);
        button2 = (TabButton)findViewById(R.id.button2);
        button3 = (TabButton)findViewById(R.id.button3);
        button4 = (TabButton)findViewById(R.id.button4);

        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        tabBitmap = new Bitmap[8];
        tabBitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        tabBitmap[1] = BitmapFactory.decodeResource(getResources(), R.drawable.map1);
        tabBitmap[2] = BitmapFactory.decodeResource(getResources(), R.drawable.information);
        tabBitmap[3] = BitmapFactory.decodeResource(getResources(), R.drawable.information1);
        tabBitmap[4] = BitmapFactory.decodeResource(getResources(), R.drawable.account);
        tabBitmap[5] = BitmapFactory.decodeResource(getResources(), R.drawable.account1);
        tabBitmap[6] = BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
        tabBitmap[7] = BitmapFactory.decodeResource(getResources(), R.drawable.alarm1);

        for(int i = 0; i < tabBitmap.length; i++)
            if(tabBitmap[i] == null)
                Log.i("bitmap","null:"+i);

        button2.bitmap = tabBitmap[3];
        button2.TAG = "资讯";
        button2.paint.setColor(0xff757575);

        button3.bitmap = tabBitmap[5];
        button3.TAG = "账户";
        button3.paint.setColor(0xff757575);

        button4.bitmap = tabBitmap[7];
        button4.paint.setColor(0xff757575);
        button4.TAG = "排队";

        imageHelper = new ImageHelper(this);
    }
}
