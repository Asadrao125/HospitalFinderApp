package com.gexton.hospitalfinderapp;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.gexton.hospitalfinderapp.gps.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TrackingAndRuoteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String name, address;
    double cLatitude, cLongitude, hLatitude, hLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_and_ruote);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        name = getIntent().getStringExtra("name");
        address = getIntent().getStringExtra("address");
        hLatitude = getIntent().getDoubleExtra("lat", 1000);
        hLongitude = getIntent().getDoubleExtra("lng", 1000);

        getCurrentLocation();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng myLatlng = new LatLng(cLatitude, cLongitude);
        mMap.addMarker(new MarkerOptions().position(myLatlng).title("My Location"));

        LatLng hospitalLatlng = new LatLng(hLatitude, hLongitude);
        mMap.addMarker(new MarkerOptions().position(hospitalLatlng).title("Hospital Locatioon"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 13.0f));
    }
}