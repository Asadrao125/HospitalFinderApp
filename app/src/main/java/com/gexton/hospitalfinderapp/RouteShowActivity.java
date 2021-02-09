package com.gexton.hospitalfinderapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.gexton.hospitalfinderapp.gps.GPSTracker;
import com.gexton.hospitalfinderapp.tracking_files.NavigationActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouteShowActivity extends BaseActivity {
    GoogleMap mMap;
    String serverKey = "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8";
    String name, address;
    double hLat, hLong, cLatitude, cLongitude;
    double total_distance, total_duration;
    TextView tv_duration, tv_distance;
    Button btn_navigate;
    String MY_PREFS_NAME = "HospitalFinder";
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_show);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red, this.getTheme()));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                getCurrentLocation();
                mMap = googleMap;
                drawRoutes(mMap);
            }
        });

        tv_distance = findViewById(R.id.tv_distance);
        tv_duration = findViewById(R.id.tv_duration);
        btn_navigate = findViewById(R.id.btn_navigate);

        adView = findViewById(R.id.adView);
        /*adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/
        AdUtil adUtil = new AdUtil(RouteShowActivity.this);
        adUtil.loadBannerAd(adView);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        name = prefs.getString("name", "NoValue");
        address = prefs.getString("address", "NoValue");
        cLatitude = Double.parseDouble(prefs.getString("cLat", "NoValue"));
        cLongitude = Double.parseDouble(prefs.getString("cLong", "NoValue"));
        hLat = Double.parseDouble(prefs.getString("hLat", "NoValue"));
        hLong = Double.parseDouble(prefs.getString("hLong", "NoValue"));

        Log.d("Location_Data_Route", "onCreate: " + name);
        Log.d("Location_Data_Route", "onCreate: " + address);
        Log.d("Location_Data_Route", "onCreate: " + cLatitude);
        Log.d("Location_Data_Route", "onCreate: " + cLongitude);
        Log.d("Location_Data_Route", "onCreate: " + hLat);
        Log.d("Location_Data_Route", "onCreate: " + hLong);

        setTitle(getString(R.string.possible_route_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
                if (gpsTracker.canGetLocation()) {
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_permission_required), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            GPSTracker gps = new GPSTracker(getApplicationContext());

            if (gps.canGetLocation()) {

                cLatitude = gps.getLatitude();
                cLongitude = gps.getLongitude();
                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + cLatitude + "\nLong: " + cLongitude, Toast.LENGTH_LONG).show();

            } else {
                gps.showSettingsAlert();
            }
        }
    }

    private void drawRoutes(final GoogleMap mMap) {
        try {
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_icon_new))
                    .title(getString(R.string.my_location_title_for_marker))
                    .position(new LatLng(cLatitude, cLongitude)));

            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.hospital))
                    .title(name)
                    .snippet(address)
                    .position(new LatLng(hLat, hLong)));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            boolean success = mMap.setMapStyle(new MapStyleOptions(MapStyleJSON.MAP_STYLE_JSON));
            //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RouteShowActivity.this, R.raw.style_json));

        } catch (Exception e) {
            e.printStackTrace();
        }

        GoogleDirection.withServerKey(serverKey)
                .from(new LatLng(cLatitude, cLongitude))
                // .and(markerPoints)
                .to(new LatLng(hLat, hLong))
                .alternativeRoute(true)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(@Nullable Direction direction) {
                        if (direction.isOK()) {

                            if (direction.getRouteList().get(0).getTotalDistance() < 1000) {
                                total_distance = direction.getRouteList().get(0).getTotalDistance();
                                tv_distance.setText(getString(R.string.distance) + ": " + total_distance + " meter");
                            } else {
                                total_distance = direction.getRouteList().get(0).getTotalDistance() / 1000;
                                tv_distance.setText(getString(R.string.distance) + ": " + total_distance + " km");
                            }

                            total_duration = direction.getRouteList().get(0).getTotalDuration() / 60;
                            tv_duration.setText(getString(R.string.duration) + ": " + total_duration + " mins");

                            for (int index = 0; index < direction.getRouteList().size(); index++) {

                                Route route = direction.getRouteList().get(index);
                                Leg leg = route.getLegList().get(0);

                                List<Step> stepList = leg.getStepList();
                                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList,
                                        5, Color.parseColor("#" + getRandom()), 3, Color.BLUE);

                                for (PolylineOptions polylineOption : polylineOptionList) {
                                    mMap.addPolyline(polylineOption);

                                }
                            }//end loop
                            setCameraWithCoordinationBounds(direction.getRouteList().get(0), mMap);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                    }
                });
    }

    private void setCameraWithCoordinationBounds(Route route, GoogleMap mMap) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    public static String getRandom() {
        ArrayList<String> list = new ArrayList<>();
        list.add("C0C0C0");
        list.add("808080");
        list.add("FF0000");
        list.add("800000");
        list.add("FFFF00");
        list.add("808000");
        list.add("00FF00");
        list.add("008000");
        list.add("00FFFF");
        list.add("0000FF");
        list.add("000080");
        list.add("FF00FF");
        list.add("800080");
        list.add("800080");

        int rnd = new Random().nextInt(list.size());
        return list.get(rnd);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_location_green);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}