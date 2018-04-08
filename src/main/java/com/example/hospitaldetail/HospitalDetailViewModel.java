package com.example.hospitaldetail;

import android.databinding.ObservableField;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HospitalDetailViewModel {
    private Bundle mData;
    private HospitalDetailView mHospitalDetailView;

    public ObservableField<String> hospitalName = new ObservableField<>();
    public ObservableField<String> hospitalPhoneNumber = new ObservableField<>();
    public ObservableField<String> hospitalAddress = new ObservableField<>();

    public HospitalDetailViewModel(Bundle data, HospitalDetailView hospitalDetailView) {
        this.mData = data;
        mHospitalDetailView = hospitalDetailView;
        hospitalName.set(mData.getString("name"));
        hospitalAddress.set(mData.getString("address"));
        hospitalPhoneNumber.set(mData.getString("phoneNum"));
    }

    public void onPhoneNumberClick(View view) {
        TextView textView = (TextView) view;
        mHospitalDetailView.dialPhoneNumber(textView.getText().toString());
    }

    public void onAddressClick() {
        mHospitalDetailView.startBaiduMap();
    }

    public void onReserveClick() {
        mHospitalDetailView.goToReserveActivity();
    }
}
