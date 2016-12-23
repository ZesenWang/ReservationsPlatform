package com.example.dentalhospital;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utils.JSONHelper;
import com.example.utils.UserInfoTask;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by wangz on 2016/10/28.
 */

public class WelcomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Tencent mTencent;
    String APP_ID = "101367274";
    RelativeLayout relativeLayout;
    EditText name, id, insuranceId, password;
    RadioGroup gender;
    Spinner spinner;
    String insuranceIdCode;
    ImageView userPhoto;
    TextView userName, password_TextView;
    ProgressDialog dialog;
    JSONObject object;
    JSONHelper helper;
    EditText editText;
    AlertDialog mAlertDialog;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            helper.interact(MainActivity.SERVER_URL + "/SignUpServlet");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcomeactivity);
        mTencent = Tencent.createInstance(APP_ID, this.getApplicationContext());
        relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

        editText = new EditText(WelcomeActivity.this);
        editText.setWidth(500);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isSetServer = preferences.getBoolean("isSetServer", false);
        if(!isSetServer) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请输入服务器URL：")
                    .setView(editText)
                    .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.SERVER_URL = editText.getText().toString();
                            preferences.edit().putBoolean("isSetServer", true).apply();
                        }
                    })
                    .setNegativeButton("我就看看，不说话", null);
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, signInlistener);
        if(requestCode == Constants.REQUEST_API) {
            Tencent.handleResultData(data, signInlistener);
        }
    }

    public void initializeSignUp(){
        setContentView(R.layout.welcomeactivity_sign_up);

        password_TextView = (TextView)findViewById(R.id.password_textView);
        password = (EditText)findViewById(R.id.password);
        name  = (EditText)findViewById(R.id.editText3);
        id = (EditText)findViewById(R.id.editText5);
        insuranceId = (EditText)findViewById(R.id.editText4);
        gender = (RadioGroup)findViewById(R.id.radio);
        spinner = (Spinner)findViewById(R.id.spinner);
        userPhoto = (ImageView)findViewById(R.id.imageView8);
        userName = (TextView)findViewById(R.id.textView15);
    }

    public void onSignUp(View view){
        initializeSignUp();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.signin_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Let me see");
        spinner.setOnItemSelectedListener(this);
    }

    public void onSubmit(View view){
        dialog = ProgressDialog.show(this, null,"与服务器通信中", true);

        String sPassword = password.getText().toString();
        final String sName = name.getText().toString();
        final String sId = id.getText().toString();
        final String sInsuranceId = insuranceId.getText().toString();
        final int genderCode = gender.getCheckedRadioButtonId();
        byte[] encriptedPassword = null;
        if(!sPassword.equals("")){
            encriptedPassword = encript(sPassword);
        }
        object = new JSONObject();
        try {
            if(encriptedPassword == null)
                object.put("sPassword","");
            else
                object.put("sPassword", encriptedPassword);
            object.put("sName",sName);
            object.put("sId",sId);
            object.put("sInsuranceId",sInsuranceId);
            object.put("genderCode",genderCode);
            object.put("insuranceCode",insuranceIdCode);
            helper = new JSONHelper(object);
            helper.setOnReceiveJSONListener(new JSONHelper.OnReceiveJSONListener() {
                @Override
                public void onReceive(JSONObject result) {
                    if(result == null){
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("sName",sName);
                        editor.putString("sId",sId);
                        editor.putString("sInsuranceId",sInsuranceId);
                        editor.putInt("genderCode",genderCode);
                        editor.putString("insuranceCode",insuranceIdCode);
                        editor.putBoolean("isSignIn", true);
                        editor.apply();

                        dialog.dismiss();
                        Toast.makeText(WelcomeActivity.this, "当前未设置服务器地址", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    try {
                        if(result.getBoolean("isSucceed")){
                            dialog.dismiss();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(WelcomeActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("sName",sName);
                            editor.putString("sId",sId);
                            editor.putString("sInsuranceId",sInsuranceId);
                            editor.putInt("genderCode",genderCode);
                            editor.putString("insuranceCode",insuranceIdCode);
                            editor.putBoolean("isSignIn", true);
                            editor.putString("serverURL", MainActivity.SERVER_URL);
                            editor.apply();
                            finish();
                        }else{
                            dialog.dismiss();
                            Toast.makeText(WelcomeActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            //让对话框先显示两秒，显得真实
            handler.sendEmptyMessageDelayed(0x123, 2000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] encript(String message){
        byte [] encription = null;
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            encription = messageDigest.digest(message.getBytes());
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return encription;
    }
    public void onQQSignIn(View view){
        mTencent.login(this, "all", signInlistener);
    }

    IUiListener signInlistener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            Log.i("fuckTencent","onComplete");
            JSONObject jsonObject = (JSONObject)o;
            try{
                String access_token = jsonObject.getString("access_token");
                String openid = jsonObject.getString(Constants.PARAM_OPEN_ID);
                String expires_in = jsonObject.getString("expires_in");

                mTencent.setOpenId(openid);
                mTencent.setAccessToken(access_token, expires_in);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("access_token",access_token);
                editor.putString("openid",openid);
                editor.putString("expires_in",expires_in);
                editor.apply();

                UserInfo info = new UserInfo(null, mTencent.getQQToken());
                info.getUserInfo(userInfoListener);
            }catch (JSONException e) {
                e.printStackTrace();
            }
            relativeLayout.removeAllViews();
            setContentView(R.layout.welcomeactivity_sign_up);
            initializeSignUp();
            password_TextView.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(UiError uiError) {
            Log.i("fuckTencent",uiError.errorDetail);
        }

        @Override
        public void onCancel() {
            Log.i("fuckTencent","onCancel");
        }
    };
    IUiListener userInfoListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            JSONObject jsonObject = (JSONObject)o;
            try {
                Log.i("json",String.valueOf(jsonObject));
                String nickname = jsonObject.getString("nickname");
                userName.setText(nickname);
                URL figureurl = new URL(jsonObject.getString("figureurl_qq_2"));
                new UserInfoTask().execute(figureurl, userPhoto);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        insuranceIdCode = Integer.toString(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
