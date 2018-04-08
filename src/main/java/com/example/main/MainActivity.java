package com.example.main;

import android.Manifest;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.activity.R;
import com.example.main.fragment.Tab1Fragment;
import com.example.main.fragment.Tab2Fragment;
import com.example.main.fragment.Tab3Fragment;
import com.example.main.fragment.Tab4Fragment;
import com.example.login.WelcomeActivity;
import com.example.service.CheckBindingService;
import com.example.utils.ImageHelper;
import com.example.utils.MyLocation;
import com.example.utils.TabButton;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity{

    private static final String TAG = "ReservationApp";
    public static final String SEARCH_KEYWORD = "口腔医院";
    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    public static String SERVER_URL = "";

    @BindColor(R.color.colorPrimary) int colorPrimary;
    @BindColor(R.color.colorLight) int colorLight;
    @BindView(R.id.button) TabButton button;
    @BindView(R.id.button2) TabButton button2;
    @BindView(R.id.button3) TabButton button3;
    @BindView(R.id.button4) TabButton button4;

    Tab1Fragment tab1Fragment;
    Tab2Fragment tab2Fragment;
    Tab3Fragment tab3Fragment;
    Tab4Fragment tab4Fragment;
    FragmentManager fragmentManager;

    Window window;
    WindowManager windowManager;
    Bitmap tabBitmap[];
    PoiSearch poiSearch;
    MapView mapView;
    boolean isSignIn;
    SharedPreferences preferences;
    public ImageHelper imageHelper;
    Intent intent;
    CheckBindingService.MyBinder binder;
    public MyLocation myLocation;
    private ImageView startingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: MainActivity");

        SDKInitializer.initialize(getApplicationContext());

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initializeMap();
        initializeFragment();
        initializeImage();
        initializeButton();
        //如果没登陆就会启动welcome activity
        isSignIn();
        //如果没给相应权限就会弹出对话框
        isPermissionGranted();
        //当用户设置过服务器URL时(第一次打开时这里返回“”，因为还没有设置过)，
        // 取出SERVER_URL的值，在注册、挂号、检查时会使用
        SERVER_URL = preferences.getString("serverURL", "");

        //启动一个定时执行的service，用于检查挂号信息,因为广播接收者不能写成内部类，所以这个方法不行
        //intent = new Intent(this, CheckIntentService.class);
        //pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        //manager = (AlarmManager)getSystemService(Service.ALARM_SERVICE);
        //handler.sendEmptyMessageDelayed(0x123, 5000);
        startCheckerService();

        allowSetStatusBarColor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: MainActivity");

        //获取位置信息和附近的医院信息, 每次用户从后台恢复activity时可以看到最新的结果
        requestPOI();
        //异步请求位置信息, 每次用户从后台恢复activity时可以看到最新的结果
        myLocation = new MyLocation(this, tab1Fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: MainActivity");
        mapView.onResume();
        // TODO: 2016/12/17 如果System.currentTimeMillis()不对，试试SystemClock.elapsedRealtime()
        //manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), 1000, pendingIntent);
        //getActionBar().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: MainActivity");
        mapView.onPause();
        //manager.cancel(pendingIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: MainActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        handler.removeMessages(0x123);
        unbindService(conn);
    }
    //Handler could be used to communicate across threads. But here, I use handler to
    //execute loop operation
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
                    int peopleCount = preferences.getInt("peopleCount", -1);
                    int queueNumber = preferences.getInt("queueNumber", -1);
                    String doctor = preferences.getString("doctor", "");

                    tab4Fragment.waitTime.setText("预计排队时间\n\n"+waitTime+"分钟");
                    tab4Fragment.peopleNumber.setText(""+peopleCount);
                    tab4Fragment.queueNumber.setText("你的排队号码\n\n"+queueNumber);
                    tab4Fragment.reservationType.setText("你的挂号类型\n\n"+doctor);
                }
            });
        }
    };

    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {

            ArrayList<PoiInfo> list = (ArrayList<PoiInfo>) poiResult.getAllPoi();

            Log.i("info",poiResult.error + "");
            if(poiResult.error == SearchResult.ERRORNO.PERMISSION_UNFINISHED){
                poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").pageCapacity(60).keyword(SEARCH_KEYWORD));
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

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.button4})
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

                window.setStatusBarColor(colorPrimary);
                break;
            case R.id.button2:
                button2.bitmap = tabBitmap[2];
                replaceFragment(tab2Fragment);
                button2.paint.setColor(0xff3385ff);

                window.setStatusBarColor(colorPrimary);

                break;
            case R.id.button3:
                button3.bitmap = tabBitmap[4];
                replaceFragment(tab3Fragment);
                button3.paint.setColor(0xff3385ff);

                window.setStatusBarColor(colorLight);

                break;
            case R.id.button4:
                button4.bitmap = tabBitmap[6];
                replaceFragment(tab4Fragment);
                button4.paint.setColor(0xff3385ff);

                window.setStatusBarColor(colorPrimary);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //更新位置
        myLocation = new MyLocation(this, tab1Fragment);
    }

    public void initializeMap(){
        mapView = new MapView(this);
        mapView.onCreate(this, null);
    }

    public void initializeImage(){
        tabBitmap = new Bitmap[8];
        tabBitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        tabBitmap[1] = BitmapFactory.decodeResource(getResources(), R.drawable.map1);
        tabBitmap[2] = BitmapFactory.decodeResource(getResources(), R.drawable.information);
        tabBitmap[3] = BitmapFactory.decodeResource(getResources(), R.drawable.information1);
        tabBitmap[4] = BitmapFactory.decodeResource(getResources(), R.drawable.account);
        tabBitmap[5] = BitmapFactory.decodeResource(getResources(), R.drawable.account1);
        tabBitmap[6] = BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
        tabBitmap[7] = BitmapFactory.decodeResource(getResources(), R.drawable.alarm1);

//        for(int i = 0; i < tabBitmap.length; i++)
//            if(tabBitmap[i] == null)
//                Log.i("bitmap","null:"+i);
        imageHelper = new ImageHelper(this);
    }

    public void initializeButton(){
        button2.bitmap = tabBitmap[3];
        button2.TAG = "资讯";
        button2.paint.setColor(0xff757575);

        button3.bitmap = tabBitmap[5];
        button3.TAG = "账户";
        button3.paint.setColor(0xff757575);

        button4.bitmap = tabBitmap[7];
        button4.paint.setColor(0xff757575);
        button4.TAG = "排队";
    }
    public void initializeFragment(){
        tab1Fragment = new Tab1Fragment();
        tab2Fragment = new Tab2Fragment();
        tab3Fragment = new Tab3Fragment();
        tab4Fragment = new Tab4Fragment();
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, tab1Fragment).commit();
    }

    public void isSignIn(){
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        isSignIn = preferences.getBoolean("isSignIn",false);
        if(!isSignIn){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    public void isPermissionGranted(){
        String[] permissions = {Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION
        ,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (isSignIn) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this, permissions,
                        MY_PERMISSIONS_REQUEST_CODE);
            }

        }
    }

    //invoke this method after activity running, otherwise you'll get null token exception
    @Deprecated
    public void popupStartingImage(){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.welcomepage);
        startingImage = new ImageView(this);
        startingImage.setImageBitmap(bitmap);

        windowManager = getWindowManager();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.gravity = Gravity.NO_GRAVITY;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        windowManager.addView(startingImage, params);
    }
    private void startCheckerService() {
        //启动一个bind类型的service，可以与activity通信
        intent = new Intent(MainActivity.this, CheckBindingService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
        handler.sendEmptyMessageDelayed(0x123, 3000);
    }

    public void requestPOI(){
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiListener);
        poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").pageCapacity(60).keyword(SEARCH_KEYWORD));
    }
    private void allowSetStatusBarColor() {
        window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
}
