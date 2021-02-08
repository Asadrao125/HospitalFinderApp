package com.gexton.hospitalfinderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

import com.gexton.hospitalfinderapp.adapters.HospitalArrayAdapter;
import com.gexton.hospitalfinderapp.adapters.SearchAdapter;
import com.gexton.hospitalfinderapp.api.ApiCallback;
import com.gexton.hospitalfinderapp.api.ApiManager;
import com.gexton.hospitalfinderapp.fragments.FragmentHospital;
import com.gexton.hospitalfinderapp.gps.GPSTracker;
import com.gexton.hospitalfinderapp.models.HospitalBean;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity implements ApiCallback {
    ListView listView;
    private SearchAdapter myAdapter;
    ApiCallback apiCallback;
    public static ArrayList<HospitalBean> hospitalBeanArrayList;
    double lati, longi;
    EditText edt_search;
    SearchView searchView;
    AutoCompleteTextView actv;
    ArrayList<String> arrayListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = findViewById(R.id.list_view_search);
        apiCallback = SearchActivity.this;
        edt_search = findViewById(R.id.edt_search);
        searchView = findViewById(R.id.search_view);
        actv = findViewById(R.id.actv);
        arrayListName = new ArrayList<>();

        getCurrentLocation();

        getNearbyHospitalsList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, arrayListName);
        actv.setAdapter(adapter);

    }

    @Override
    public void onApiResponce(int httpStCode, int successOrFail, String apiName, String apiResponce) {

        if (apiName.equalsIgnoreCase(ApiManager.API_HOME_LIST)) {

            Log.d("Api_Response", "Api Name: " + apiName);
            Log.d("Api_Response", "Response: " + apiResponce);
            Log.d("Api_Response", "Status code: " + httpStCode);

            try {
                JSONObject jsonObject = new JSONObject(apiResponce);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                hospitalBeanArrayList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    String image = jsonArray.getJSONObject(i).getString("icon");
                    Double latitude = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double longitude = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String address = jsonArray.getJSONObject(i).getString("vicinity");

                    HospitalBean hospitalBean = new HospitalBean(name, image, latitude, longitude, address);
                    hospitalBeanArrayList.add(hospitalBean);
                    arrayListName.add(name);
                }

                myAdapter = new SearchAdapter(getApplicationContext(), hospitalBeanArrayList);
                listView.setAdapter(myAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            GPSTracker gps = new GPSTracker(getApplicationContext());

            if (gps.canGetLocation()) {

                lati = gps.getLatitude();
                longi = gps.getLongitude();

            } else {
                gps.enableLocationPopup();
            }
        }
    }

    private void getNearbyHospitalsList() {
        String lat = String.valueOf(lati);
        String lng = String.valueOf(longi);

        RequestParams requestParams = new RequestParams();
        //requestParams.put("location", "25.3689856,68.3474944");
        requestParams.put("location", lat + "," + lng);
        requestParams.put("radius", "1500");
        requestParams.put("type", "hospital");
        requestParams.put("key", "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8");

        ApiManager apiManager = new ApiManager(SearchActivity.this, "get", ApiManager.API_HOME_LIST, requestParams, apiCallback);
        apiManager.loadURL();
    }

}