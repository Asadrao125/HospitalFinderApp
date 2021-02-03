package com.gexton.hospitalfinderapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.gexton.hospitalfinderapp.R;
import com.gexton.hospitalfinderapp.models.HospitalBean;

import java.util.ArrayList;
import java.util.List;

public class ActvAdapter extends ArrayAdapter<HospitalBean> {
    private final Context mContext;
    private final List<HospitalBean> mHospitalBeans;
    private final List<HospitalBean> mHospitalBeansAll;
    private final int mLayoutResourceId;

    public ActvAdapter(Context context, int resource, List<HospitalBean> beanList) {
        super(context, resource, beanList);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mHospitalBeans = new ArrayList<>(beanList);
        this.mHospitalBeansAll = new ArrayList<>(beanList);
    }

    public int getCount() {
        return mHospitalBeans.size();
    }

    public HospitalBean getItem(int position) {
        return mHospitalBeans.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(mLayoutResourceId, parent, false);
            }
            HospitalBean department = getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.tv_result);
            name.setText(department.hospitalName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((HospitalBean) resultValue).hospitalName;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<HospitalBean> departmentsSuggestion = new ArrayList<>();
                if (constraint != null) {
                    for (HospitalBean department : mHospitalBeansAll) {
                        if (department.hospitalName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            departmentsSuggestion.add(department);
                        }
                    }
                    filterResults.values = departmentsSuggestion;
                    filterResults.count = departmentsSuggestion.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mHospitalBeans.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                    for (Object object : (List<?>) results.values) {
                        if (object instanceof HospitalBean) {
                            mHospitalBeans.add((HospitalBean) object);
                        }
                    }
                    notifyDataSetChanged();
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    mHospitalBeans.addAll(mHospitalBeansAll);
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
