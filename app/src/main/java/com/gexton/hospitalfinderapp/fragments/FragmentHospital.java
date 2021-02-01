package com.gexton.hospitalfinderapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gexton.hospitalfinderapp.R;
import com.gexton.hospitalfinderapp.RouteShowActivity;
import com.gexton.hospitalfinderapp.adapters.HospitalArrayAdapter;
import com.gexton.hospitalfinderapp.api.ApiCallback;
import com.gexton.hospitalfinderapp.api.ApiManager;
import com.gexton.hospitalfinderapp.gps.GPSTracker;
import com.gexton.hospitalfinderapp.models.HospitalBean;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class FragmentHospital extends Fragment implements ApiCallback {

    LinearLayout viewMap, viewList;
    ImageView img_map, img_list;
    TextView tv_map, tv_list;
    ApiCallback apiCallback;
    double lati, longi;
    ListView list_View;
    GoogleMap mMap;
    View view;
    private HospitalArrayAdapter myAdapter;
    LinearLayout mapview_layout, listview_layout;
    RelativeLayout layout_mapview, layout_listview;
    public static ArrayList<HospitalBean> hospitalBeanArrayList;
    String MY_PREFS_NAME = "HospitalFinder";
    Marker marker;
    double newLat, newLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_hospital, container, false);

        apiCallback = FragmentHospital.this;
        viewMap = view.findViewById(R.id.view_map);
        viewList = view.findViewById(R.id.view_list);
        img_map = view.findViewById(R.id.img_map);
        img_list = view.findViewById(R.id.img_list);
        tv_map = view.findViewById(R.id.tv_map);
        tv_list = view.findViewById(R.id.tv_list);
        mapview_layout = view.findViewById(R.id.mapview_layout);
        listview_layout = view.findViewById(R.id.listview_layout);
        list_View = view.findViewById(R.id.list_View);

        // For listener
        mapview_layout = view.findViewById(R.id.mapview_layout);
        listview_layout = view.findViewById(R.id.listview_layout);
        //For hide and show
        layout_mapview = view.findViewById(R.id.layout_mapview);
        layout_listview = view.findViewById(R.id.layout_listview);

        getCurrentLocation();

        getNearbyHospitalsList();

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

        list_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getContext(), "" + i, Toast.LENGTH_SHORT).show();
                GPSTracker gpsTracker = new GPSTracker(getContext());
                if (gpsTracker.canGetLocation()) {
                    Intent intent = new Intent(getContext(), RouteShowActivity.class);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("name", hospitalBeanArrayList.get(i).hospitalName);
                    editor.putString("address", hospitalBeanArrayList.get(i).address);
                    editor.putString("cLat", String.valueOf(lati));
                    editor.putString("cLong", String.valueOf(longi));
                    editor.putString("hLat", String.valueOf(hospitalBeanArrayList.get(i).lat));
                    editor.putString("hLong", String.valueOf(hospitalBeanArrayList.get(i).lng));
                    editor.apply();
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Please enable your location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
        return view;
    }

    private void getNearbyHospitalsList() {
        String lat = String.valueOf(lati);
        String lng = String.valueOf(longi);

        RequestParams requestParams = new RequestParams();
        requestParams.put("location", lat + "," + lng);
        requestParams.put("radius", "1500");
        requestParams.put("type", "hospital");
        requestParams.put("key", "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8");

        ApiManager apiManager = new ApiManager(getActivity(), "get", ApiManager.API_HOME_LIST, requestParams, apiCallback);
        apiManager.loadURL();
    }

    public void plotMarkersOnMap() {
        if (mMap != null) {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (int i = 0; i < hospitalBeanArrayList.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(hospitalBeanArrayList.get(i).lat, hospitalBeanArrayList.get(i).lng))
                        .title(hospitalBeanArrayList.get(i).hospitalName)
                        .snippet(hospitalBeanArrayList.get(i).address)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.hospital));

                marker = mMap.addMarker(markerOptions);
                marker.setTag(hospitalBeanArrayList.get(i));//tag set kar dya

                builder.include(marker.getPosition());//add marker to latlong bounds

                //Setting Custom Info Window
                setInfoAdapter();
                //Setting Click Listener on Info Window
                infoWindowListener();

                SharedPreferences prefs = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                if (!TextUtils.isEmpty(prefs.getString("newLat", "NoValue")) && !prefs.getString("newLat", "NoValue").equals("NoValue")) {
                    newLat = Double.parseDouble(prefs.getString("newLat", "NoValue"));
                    newLng = Double.parseDouble(prefs.getString("newLng", "NoValue"));

                    //When Lat Lng of Specific marker matches to List , it will open its info window
                    if (hospitalBeanArrayList.get(i).lat.equals(newLat) && hospitalBeanArrayList.get(i).lng.equals(newLng)) {
                        marker.showInfoWindow();
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLat, newLng), 15.0f));

                }

            }//end for loop

            LatLngBounds bounds = builder.build();
            int padding = 50;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);

            //Animate camera and Show Info Window When Click On Marker
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    HospitalBean hospitalBeanFromMArker = (HospitalBean) marker.getTag();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(hospitalBeanFromMArker.lat, hospitalBeanFromMArker.lng), 15.0f));
                    marker.showInfoWindow();
                    return true;
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mMap.animateCamera(cu);
                }
            });
        }
    }

    private void infoWindowListener() {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                GPSTracker gps = new GPSTracker(getContext());

                if (gps.canGetLocation()) {

                    HospitalBean hospitalBeanFromMArker = (HospitalBean) marker.getTag();
                    Intent intent = new Intent(getContext(), RouteShowActivity.class);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("name", hospitalBeanFromMArker.hospitalName);
                    editor.putString("address", hospitalBeanFromMArker.address);
                    editor.putString("cLat", String.valueOf(lati));
                    editor.putString("cLong", String.valueOf(longi));
                    editor.putString("hLat", String.valueOf(hospitalBeanFromMArker.lat));
                    editor.putString("hLong", String.valueOf(hospitalBeanFromMArker.lng));
                    editor.apply();
                    startActivity(intent);

                } else {
                    //gps.showSettingsAlert();
                    Toast.makeText(getContext(), "Please enable your location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setInfoAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
                ImageView hospital_image = v.findViewById(R.id.hospital_image);
                TextView hospital_name = v.findViewById(R.id.hospital_name);
                TextView tv_latitude = v.findViewById(R.id.tv_latitude);
                TextView tv_longitude = v.findViewById(R.id.tv_longitude);
                TextView tv_address = v.findViewById(R.id.tv_address);
                Button btnTrack = v.findViewById(R.id.btnTrack);
                if (marker.getTag() != null) {
                    HospitalBean hospitalBeanFromMArker = (HospitalBean) marker.getTag();
                    hospital_name.setText(hospitalBeanFromMArker.hospitalName);
                    tv_latitude.setText("" + hospitalBeanFromMArker.lat);
                    tv_longitude.setText("" + hospitalBeanFromMArker.lng);
                    hospital_image.setImageResource(R.drawable.hospital);
                    tv_address.setText(hospitalBeanFromMArker.address);
                    btnTrack.setText("Track " + hospitalBeanFromMArker.hospitalName);
                    btnTrack.setTextColor(Color.BLACK);
                }
                return v;
            }
        });
    }

    public void getCurrentLocation() {
        GPSTracker gps = new GPSTracker(getContext());
        if (gps.canGetLocation()) {
            lati = gps.getLatitude();
            longi = gps.getLongitude();
        }
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
                }

                myAdapter = new HospitalArrayAdapter(getContext(), hospitalBeanArrayList);
                //myAdapter.setAdapter and myAdapter.notifyDatasetChanged both do same work.
                // myAdapter.notifyDataSetChanged();
                list_View.setAdapter(myAdapter);

                plotMarkersOnMap();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}