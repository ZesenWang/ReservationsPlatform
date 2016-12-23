package com.example.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wangz on 2016/11/19.
 */

public class UserInfoTask extends AsyncTask<Object, Void, Bitmap> {
    ImageView userPhoto;
    @Override
    protected Bitmap doInBackground(Object... params) {
        URL url = (URL)params[0];
        userPhoto = (ImageView)params[1];
        Bitmap bitmap = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");

            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        userPhoto.setImageBitmap(bitmap);
    }
}
