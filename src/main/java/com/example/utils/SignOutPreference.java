package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.login.WelcomeActivity;

/**
 * Created by wangz on 2016/12/13.
 */

public class SignOutPreference extends DialogPreference {
    TextView textView;
    Context mContext;
    public SignOutPreference(Context context) {
        super(context);

        setDialogMessage("你确定要退出吗");
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        mContext = context;
    }

    public SignOutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogMessage("你确定要退出吗");
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        mContext = context;

    }

    public SignOutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDialogMessage("你确定要退出吗");
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        mContext = context;

    }

    public SignOutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setDialogMessage("你确定要退出吗");
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        mContext = context;

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            persistBoolean(false);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(mContext, WelcomeActivity.class);
            mContext.startActivity(intent);
        }
    }
}
