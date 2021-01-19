package com.gexton.hospitalfinderapp.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gexton.hospitalfinderapp.R;

public class FragmentPharmacies extends Fragment {
    View view;
    LinearLayout viewMap, viewList;
    ImageView img_map, img_list;
    TextView tv_map, tv_list;
    LinearLayout mapview_layout, listview_layout;
    RelativeLayout layout_mapview, layout_listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pharmacies, container, false);

        viewMap = view.findViewById(R.id.view_map);
        viewList = view.findViewById(R.id.view_list);
        img_map = view.findViewById(R.id.img_map);
        img_list = view.findViewById(R.id.img_list);
        tv_map = view.findViewById(R.id.tv_map);
        tv_list = view.findViewById(R.id.tv_list);
        mapview_layout = view.findViewById(R.id.mapview_layout);
        listview_layout = view.findViewById(R.id.listview_layout);

        // For listener
        mapview_layout = view.findViewById(R.id.mapview_layout);
        listview_layout = view.findViewById(R.id.listview_layout);
        //For hide and show
        layout_mapview = view.findViewById(R.id.layout_mapview);
        layout_listview = view.findViewById(R.id.layout_listview);

        mapview_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tv_map.setTextColor(Color.RED);
                img_map.setImageResource(R.drawable.mapview_red);
                viewMap.setBackgroundColor(Color.RED);

                tv_list.setTextColor(Color.GRAY);
                img_list.setImageResource(R.drawable.listview_grey);
                viewList.setBackgroundColor(Color.GRAY);

                layout_listview.setVisibility(View.GONE);
                layout_mapview.setVisibility(View.VISIBLE);

            }
        });

        listview_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tv_list.setTextColor(Color.RED);
                img_list.setImageResource(R.drawable.listview_red);
                viewList.setBackgroundColor(Color.RED);

                tv_map.setTextColor(Color.GRAY);
                img_map.setImageResource(R.drawable.mapview_grey);
                viewMap.setBackgroundColor(Color.GRAY);

                layout_mapview.setVisibility(View.GONE);
                layout_listview.setVisibility(View.VISIBLE);

            }
        });

        return view;
    }
}