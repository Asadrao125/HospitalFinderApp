package com.gexton.hospitalfinderapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.gexton.hospitalfinderapp.R;
import com.gexton.hospitalfinderapp.models.HospitalBean;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SearchAdapter extends BaseAdapter {

    private List<HospitalBean> itemsModelsl;
    private Context context;

    public SearchAdapter(@NonNull Context context, List<HospitalBean> itemsModelsl) {
        this.itemsModelsl = itemsModelsl;
        this.context = context;
    }

    @Override
    public int getCount() {
        return itemsModelsl.size();
    }

    @Override
    public Object getItem(int i) {
        return itemsModelsl.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospitals, null, true);
        TextView tv_hospital_name = view.findViewById(R.id.tv_hospital_name);
        TextView tv_hospital_lat = view.findViewById(R.id.tv_hospital_lat);
        TextView tv_hospital_lng = view.findViewById(R.id.tv_hospital_lng);
        TextView tv_hospital_address = view.findViewById(R.id.tv_hospital_address);
        tv_hospital_name.setText(itemsModelsl.get(position).hospitalName);
        tv_hospital_lat.setText("Latitude: " + itemsModelsl.get(position).lat);
        tv_hospital_lng.setText("Longitude: " + itemsModelsl.get(position).lng);
        tv_hospital_address.setText(itemsModelsl.get(position).address);
        return view;
    }
}
