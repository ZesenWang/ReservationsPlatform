package com.example.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dentalhospital.MainActivity;
import com.example.dentalhospital.R;
import com.example.dentalhospital.ReserveActivity;

import org.json.JSONException;

/**
 * Created by wangz on 2016/10/29.
 */

public class ReserveFragment1 extends Fragment implements AdapterView.OnItemClickListener{
    TextView textView;
    ListView listView;
    ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reserve_page_1, container, false);
        textView = (TextView)view.findViewById(R.id.textView16);
        listView = (ListView)view.findViewById(R.id.listView);
        textView.setText("请选择科室");

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.department, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ReserveActivity activity = (ReserveActivity)getActivity();
        try {
            activity.reservationInfo.put("department",position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, new ReserveFragment2());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
