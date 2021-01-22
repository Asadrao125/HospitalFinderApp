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

public class SearchAdapter extends BaseAdapter implements Filterable {

    private List<HospitalBean> itemsModelsl;
    private List<HospitalBean> itemsModelListFiltered;
    private Context context;

    public SearchAdapter(@NonNull Context context, List<HospitalBean> itemsModelsl) {
        //super(context, R.layout.item_hospitals, itemsModelsl);
        this.itemsModelsl = itemsModelsl;
        this.itemsModelListFiltered = itemsModelsl;
        this.context = context;
    }

    @Override
    public int getCount() {
        return itemsModelListFiltered.size();
    }

    @Override
    public Object getItem(int i) {
        return itemsModelListFiltered.get(i);
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
        tv_hospital_name.setText(itemsModelListFiltered.get(position).hospitalName);
        tv_hospital_lat.setText("Latitude: " + itemsModelListFiltered.get(position).lat);
        tv_hospital_lng.setText("Longitude: " + itemsModelListFiltered.get(position).lng);
        tv_hospital_address.setText(itemsModelListFiltered.get(position).address);
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filterResults.count = itemsModelsl.size();
                    filterResults.values = itemsModelsl;
                } else {
                    List<HospitalBean> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();

                    /*if (itemsModel.hospitalName.contains(searchStr) || itemsModel.address.contains(searchStr))*/

                    for (HospitalBean itemsModel : itemsModelsl) {
                        if (itemsModel.hospitalName.contains(searchStr)) {
                            resultsModel.add(itemsModel);

                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                itemsModelListFiltered = (List<HospitalBean>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
