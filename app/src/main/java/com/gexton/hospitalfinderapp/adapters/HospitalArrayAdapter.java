package com.gexton.hospitalfinderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gexton.hospitalfinderapp.DashbordActivity;
import com.gexton.hospitalfinderapp.R;
import com.gexton.hospitalfinderapp.RuoteAndTrackActivity;
import com.gexton.hospitalfinderapp.TrackingAndRuoteActivity;
import com.gexton.hospitalfinderapp.models.HospitalBean;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HospitalArrayAdapter extends ArrayAdapter<HospitalBean> {
    Context context;
    List<HospitalBean> hospitalBeanList;

    public HospitalArrayAdapter(@NonNull Context context, List<HospitalBean> hospitalBeanList) {
        super(context, R.layout.item_hospitals, hospitalBeanList);
        this.context = context;
        this.hospitalBeanList = hospitalBeanList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospitals, null, true);
        TextView tv_hospital_name = view.findViewById(R.id.tv_hospital_name);
        //ImageView img_hospital = view.findViewById(R.id.img_hospital);
        TextView tv_hospital_lat = view.findViewById(R.id.tv_hospital_lat);
        TextView tv_hospital_lng = view.findViewById(R.id.tv_hospital_lng);
        TextView tv_hospital_address = view.findViewById(R.id.tv_hospital_address);
        ImageView img_direction = view.findViewById(R.id.img_direction);

        tv_hospital_name.setText(hospitalBeanList.get(position).hospitalName);
        tv_hospital_lat.setText("Latitude: " + hospitalBeanList.get(position).lat);
        tv_hospital_lng.setText("Longitude: " + hospitalBeanList.get(position).lng);
        //Picasso.get().load(hospitalBeanList.get(position).imageHospital).into(img_hospital);
        tv_hospital_address.setText(hospitalBeanList.get(position).address);
        img_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "" + hospitalBeanList.get(position).lat + "\n" + hospitalBeanList.get(position).lng, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, RuoteAndTrackActivity.class);
                intent.putExtra("name", hospitalBeanList.get(position).hospitalName);
                intent.putExtra("address", hospitalBeanList.get(position).address);
                intent.putExtra("lat", hospitalBeanList.get(position).lat);
                intent.putExtra("lng", hospitalBeanList.get(position).lng);
                context.startActivity(intent);
            }
        });


        return view;
    }
}