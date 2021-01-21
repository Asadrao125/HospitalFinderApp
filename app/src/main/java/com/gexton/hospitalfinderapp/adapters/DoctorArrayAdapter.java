package com.gexton.hospitalfinderapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gexton.hospitalfinderapp.R;
import com.gexton.hospitalfinderapp.models.HospitalBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DoctorArrayAdapter extends ArrayAdapter<HospitalBean> {
    Context context;
    List<HospitalBean> hospitalBeanList;

    public DoctorArrayAdapter(@NonNull Context context, List<HospitalBean> hospitalBeanList) {
        super(context, R.layout.item_hospitals, hospitalBeanList);
        this.context = context;
        this.hospitalBeanList = hospitalBeanList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospitals, null, true);
        TextView tv_hospital_name = view.findViewById(R.id.tv_hospital_name);
        ImageView img_hospital = view.findViewById(R.id.img_hospital);
        TextView tv_hospital_lat = view.findViewById(R.id.tv_hospital_lat);
        TextView tv_hospital_lng = view.findViewById(R.id.tv_hospital_lng);
        TextView tv_hospital_address = view.findViewById(R.id.tv_hospital_address);

        tv_hospital_name.setText(hospitalBeanList.get(position).hospitalName);
        tv_hospital_lat.setText("Latitude: " + hospitalBeanList.get(position).lat);
        tv_hospital_lng.setText("Longitude: " + hospitalBeanList.get(position).lng);
        tv_hospital_address.setText(hospitalBeanList.get(position).address);
        img_hospital.setImageResource(R.drawable.doctor);

        return view;
    }
}
