package com.example.hospitaldetail;

import android.databinding.ObservableField;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HDViewModel {
    private Bundle mData;
    private HDView mHDView;

    public ObservableField<String> hospitalName = new ObservableField<>();
    public ObservableField<String> hospitalPhoneNumber = new ObservableField<>();
    public ObservableField<String> hospitalAddress = new ObservableField<>();

    public HDViewModel(Bundle data, HDView hdView) {
        this.mData = data;
        mHDView = hdView;
        hospitalName.set(mData.getString("name"));
        hospitalAddress.set(mData.getString("address"));
        hospitalPhoneNumber.set(mData.getString("phoneNum"));
    }

    public void onPhoneNumberClick(View view) {
        TextView textView = (TextView) view;
        mHDView.dialPhoneNumber(textView.getText().toString());
    }

    public void onAddressClick() {
        mHDView.startBaiduMap();
    }

    public void onReserveClick() {
        mHDView.goToReserveActivity();
    }
}
