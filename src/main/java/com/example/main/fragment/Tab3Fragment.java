package com.example.main.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.activity.R;

/**
 * Created by wangz on 2016/9/25.
 */
public class Tab3Fragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_screen_main_activity_fragment3);
    }
}
