package com.gexton.hospitalfinderapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.gexton.hospitalfinderapp.adapters.ViewPagerAdapter;
import com.gexton.hospitalfinderapp.fragments.FragmentDoctors;
import com.gexton.hospitalfinderapp.fragments.FragmentHospital;
import com.gexton.hospitalfinderapp.fragments.FragmentPharmacies;
import com.google.android.material.tabs.TabLayout;

public class DashbordActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    FragmentHospital fragmentHospital;
    FragmentDoctors fragmentDoctors;
    FragmentPharmacies fragmentPharmacies;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    Toolbar toolbar;

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
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();
        toolbar = findViewById(R.id.toolbar);

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
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            dl.closeDrawers();
            viewPager.setCurrentItem(0);
        }
    }

}