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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashbordActivity extends AppCompatActivity implements ApiCallback {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    ImageView img_drawer, img_search, img_drawer2;
    AutoCompleteTextView actv;
    public RelativeLayout layout_search;
    RelativeLayout contentFrame;

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

    OnSwipeTouchListener onSwipeTouchListener;

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
        find_hospital_layout = findViewById(R.id.find_hospital_layout);
        find_doctor_layout = findViewById(R.id.find_doctor_layout);
        find_pharmacy_layout = findViewById(R.id.find_pharmacy_layout);
        share_to_a_friend = findViewById(R.id.share_to_a_friend);
        rate_app = findViewById(R.id.rate_app);
        img_search = findViewById(R.id.img_search);
        arrayListName = new ArrayList<>();
        apiCallback = DashbordActivity.this;
        img_drawer2 = findViewById(R.id.img_drawer2);
        img_drawer = findViewById(R.id.img_drawer);
        actv = findViewById(R.id.actv);
        layout_search = findViewById(R.id.layout_search);
        contentFrame = findViewById(R.id.contentFrame);

        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_search.setVisibility(View.VISIBLE);
                actv.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(actv, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        //Animation Sliding Activity
        dl.setScrimColor(Color.TRANSPARENT);
        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                contentFrame.setTranslationX(slideX);
            }
        };//Animation Sliding Ends here

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        getNearbyHospitalsList("hospital");

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int current_tab = tab.getPosition();
                if (current_tab == 0) {
                    getNearbyHospitalsList("hospital");
                } else if (current_tab == 1) {
                    getNearbyHospitalsList("doctor");
                } else if (current_tab == 2) {
                    getNearbyHospitalsList("pharmacy");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        onSwipeTouchListener = new OnSwipeTouchListener(this, findViewById(R.id.layout_search), findViewById(R.id.actv));

        img_drawer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dl.isDrawerOpen(GravityCompat.START)) {
                    dl.closeDrawers();
                } else {
                    dl.openDrawer(GravityCompat.START);
                }
            }
        });
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
        if (apiName.equalsIgnoreCase(ApiManager.API_HOME_LIST)) {
            try {
                JSONObject jsonObject = new JSONObject(apiResponce);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                Log.d("name_list", "Json Array Response: " + jsonArray);
                arrayListName.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String name = jsonArray.getJSONObject(i).getString("name");
                    arrayListName.add(name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListName);
                actv.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashbordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            GPSTracker gps = new GPSTracker(DashbordActivity.this);
            if (gps.canGetLocation()) {
                lati = gps.getLatitude();
                longi = gps.getLongitude();
            } else {
                gps.enableLocationPopup();
            }
        }
        Log.d("name_list", "getCurrentLocation: Current Location_method");
    }

    private void getNearbyHospitalsList(String type) {
        String lat = String.valueOf(lati);
        String lng = String.valueOf(longi);

        RequestParams requestParams = new RequestParams();
        requestParams.put("location", lat + "," + lng);
        requestParams.put("radius", "1500");
        requestParams.put("type", type);
        requestParams.put("key", "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8");

        ApiManager apiManager = new ApiManager(DashbordActivity.this, "get", ApiManager.API_HOME_LIST, requestParams, apiCallback);
        apiManager.loadURL();

        Log.d("name_list", "getNearbyHospitalsList: get near by hospital method");

    }

    @Override
    protected void onResume() {
        super.onResume();
        layout_search.setVisibility(View.GONE);
    }

    static class OnSwipeTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;
        Context context;
        View mav;
        View ac;

        OnSwipeTouchListener(Context ctx, View mainView, View ac1) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            mainView.setOnTouchListener(this);
            context = ctx;
            mav = mainView;
            ac = ac1;

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        void onSwipeRight() {
            mav.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ac.getWindowToken(), 0);
            this.onSwipe.swipeRight();
        }

        void onSwipeLeft() {
            mav.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ac.getWindowToken(), 0);
            this.onSwipe.swipeLeft();
        }

        void onSwipeTop() {
            mav.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ac.getWindowToken(), 0);
            this.onSwipe.swipeTop();
        }

        void onSwipeBottom() {
            mav.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ac.getWindowToken(), 0);
            this.onSwipe.swipeBottom();
        }

        interface onSwipeListener {
            void swipeRight();

            void swipeTop();

            void swipeBottom();

            void swipeLeft();
        }

        onSwipeListener onSwipe;
    }

}