package com.gexton.hospitalfinderapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gexton.hospitalfinderapp.adapters.SearchAdapter;
import com.gexton.hospitalfinderapp.adapters.ViewPagerAdapter;
import com.gexton.hospitalfinderapp.api.ApiCallback;
import com.gexton.hospitalfinderapp.api.ApiManager;
import com.gexton.hospitalfinderapp.fragments.FragmentDoctors;
import com.gexton.hospitalfinderapp.fragments.FragmentHospital;
import com.gexton.hospitalfinderapp.fragments.FragmentPharmacies;
import com.gexton.hospitalfinderapp.gps.GPSTracker;
import com.gexton.hospitalfinderapp.models.HospitalBean;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashbordActivity extends AppCompatActivity implements ApiCallback {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    RelativeLayout main_view;
    ImageView img_drawer, img_search, img_drawer2;
    AutoCompleteTextView actv;
    RelativeLayout layout_search;

    FragmentHospital fragmentHospital;
    FragmentDoctors fragmentDoctors;
    FragmentPharmacies fragmentPharmacies;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    Toolbar toolbar;

    ArrayList<String> arrayListName;
    double lati, longi;
    ApiCallback apiCallback;

    LinearLayout find_hospital_layout, find_doctor_layout, find_pharmacy_layout, share_to_a_friend, rate_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbord);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red, this.getTheme()));
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        dl = (DrawerLayout) findViewById(R.id.activity_main);
        main_view = findViewById(R.id.main_view);
        img_drawer = findViewById(R.id.img_drawer);
        find_hospital_layout = findViewById(R.id.find_hospital_layout);
        find_doctor_layout = findViewById(R.id.find_doctor_layout);
        find_pharmacy_layout = findViewById(R.id.find_pharmacy_layout);
        share_to_a_friend = findViewById(R.id.share_to_a_friend);
        rate_app = findViewById(R.id.rate_app);
        img_search = findViewById(R.id.img_search);
        arrayListName = new ArrayList<>();
        apiCallback = DashbordActivity.this;
        img_drawer2 = findViewById(R.id.img_drawer2);
        actv = findViewById(R.id.actv);
        layout_search = findViewById(R.id.layout_search);

        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                /*actv.setVisibility(View.VISIBLE);
                img_search.setVisibility(View.GONE);*/
                layout_search.setVisibility(View.VISIBLE);
            }
        });

        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close) {
            //private float scaleFactor = 4f;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                /*main_view.setTranslationX(slideOffset * drawerView.getWidth());
                dl.setScrimColor(Color.TRANSPARENT);
                dl.bringChildToFront(drawerView);
                dl.requestLayout();*/

                /*float slideX = drawerView.getWidth() * slideOffset;
                main_view.setTranslationX(slideX);
                main_view.setScaleX(1 - (slideOffset / scaleFactor));
                main_view.setScaleY(1 - (slideOffset / scaleFactor));*/

            }
        };

        dl.setDrawerListener(t);
        dl.addDrawerListener(t);
        t.syncState();
        toolbar = findViewById(R.id.toolbar);

        img_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dl.closeDrawers();
            }
        });

        find_hospital_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                dl.closeDrawers();
            }
        });

        find_doctor_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                dl.closeDrawers();
            }
        });

        find_pharmacy_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
                dl.closeDrawers();
            }
        });

        share_to_a_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIntent();
                dl.closeDrawers();
            }
        });

        rate_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIntent();
                dl.closeDrawers();
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Make toolbar show navigation button (i.e back button with arrow icon)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_format_list_bulleted_24);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViewPager(viewPager);

        getCurrentLocation();

        getNearbyHospitalsList();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        fragmentHospital = new FragmentHospital();
        fragmentDoctors = new FragmentDoctors();
        fragmentPharmacies = new FragmentPharmacies();
        adapter.addFragment(fragmentHospital, "HOSPITAL");
        adapter.addFragment(fragmentDoctors, "DOCTOR");
        adapter.addFragment(fragmentPharmacies, "PHARMACY");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0 && !dl.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(0);
            dl.closeDrawers();
        }
    }

    public void shareIntent() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hospital FInder App");
            String shareMessage = "Let me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, ""));
        } catch (Exception e) {
            e.toString();
        }
    }

    @Override
    public void onApiResponce(int httpStCode, int successOrFail, String apiName, String apiResponce) {
        Log.d("name_list", "onApiResponce1: ");
        if (apiName.equalsIgnoreCase(ApiManager.API_HOME_LIST)) {
            Log.d("name_list", "onApiResponce2: ");
            try {
                Log.d("name_list", "onApiResponce3: ");
                JSONObject jsonObject = new JSONObject(apiResponce);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                Log.d("name_list", "Json Array Response: " + jsonArray);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    arrayListName.add(name);
                    Log.d("name_list", "onApiResponce4: " + name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, arrayListName);
                actv.setAdapter(adapter);
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
        Log.d("name_list", "getCurrentLocation: Current Location_method");
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

        ApiManager apiManager = new ApiManager(DashbordActivity.this, "get", ApiManager.API_HOME_LIST, requestParams, apiCallback);
        apiManager.loadURL();

        Log.d("name_list", "getNearbyHospitalsList: get near by hospital method");

    }

}