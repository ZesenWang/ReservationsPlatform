package com.example.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by wangz on 2016/11/20.
 */

public class JSONHelper {
    public JSONObject object;
    public JSONObject resultJSON;
    public String file;
    public OnReceiveJSONListener listener;

    public JSONHelper(JSONObject o){
        object = o;
        Log.i("json","constructor");
    }
    public void setOnReceiveJSONListener(OnReceiveJSONListener listener){
        this.listener = listener;
        Log.i("json","listener");
    }
    public void interact(String path){
        this.file = path;
        if(!file.startsWith("http")){
            listener.onReceive(null);
            return ;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("json","inThread");
                HttpURLConnection conn;
                OutputStream out = null;
                InputStream in = null;
                JSONObject result = null;
                try {
                    URL url = new URL(file);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    out = conn.getOutputStream();
                    out.write(object.toString().getBytes());
                    Log.i("json","response code:"+conn.getResponseCode());
                    Log.i("json","response message:"+conn.getResponseMessage());

                    in = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    char jsonString[] = new char[5];
                    StringBuilder stringBuilder = new StringBuilder();
                    while(reader.read(jsonString) != -1){
                        stringBuilder.append(jsonString);
                    }
                    Log.i("json","raw json string:"+stringBuilder.toString());
                    result = new JSONObject(stringBuilder.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    if(out != null){
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                resultJSON = result;
                listener.onReceive(resultJSON);
            }
        }).start();
    }
    public interface OnReceiveJSONListener{
        void onReceive(JSONObject result);
    }
}
