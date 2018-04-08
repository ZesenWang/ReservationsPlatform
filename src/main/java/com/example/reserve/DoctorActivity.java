package com.example.reserve;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.activity.R;

public class DoctorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
    }

    public void onClickNote(View view) {
        setContentView(R.layout.activity_note);
    }

    public void onClickReservation(View view) {
        setContentView(R.layout.activity_doctor);
    }

    public void onClickNews(View view) {
        setContentView(R.layout.activity_news);
    }
}
