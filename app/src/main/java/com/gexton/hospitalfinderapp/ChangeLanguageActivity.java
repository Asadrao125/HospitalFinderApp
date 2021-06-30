package com.gexton.hospitalfinderapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

import androidx.annotation.NonNull;

public class ChangeLanguageActivity extends BaseActivity {
    RelativeLayout layout_sindhi, layout_urdu, layout_english;
    RadioButton rb_sindhi, rb_urdu, rb_english;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.red, this.getTheme()));
        }

        layout_sindhi = findViewById(R.id.layout_sindhi);
        layout_urdu = findViewById(R.id.layout_urdu);
        layout_english = findViewById(R.id.layout_english);
        rb_sindhi = findViewById(R.id.rb_sindhi);
        rb_urdu = findViewById(R.id.rb_urdu);
        rb_english = findViewById(R.id.rb_eng);

        setTitle(getString(R.string.change_language));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adView = findViewById(R.id.adView);
        AdUtil adUtil = new AdUtil(ChangeLanguageActivity.this);
        adUtil.loadBannerAd(adView);

        String lang = prefs.getString("lang_key", "en");
        System.out.println("-- oncreate lang " + lang);

        if (lang.equalsIgnoreCase("en")) {
            rb_english.setChecked(true);
            rb_urdu.setChecked(false);
            rb_sindhi.setChecked(false);
        } else if (lang.equalsIgnoreCase("ur")) {
            rb_english.setChecked(false);
            rb_urdu.setChecked(true);
            rb_sindhi.setChecked(false);
        } else if (lang.equalsIgnoreCase("si")) {
            rb_english.setChecked(false);
            rb_urdu.setChecked(false);
            rb_sindhi.setChecked(true);
        }

        layout_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("en");
            }
        });

        layout_urdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("ur");
            }
        });

        layout_sindhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("si");
            }
        });
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        conf.locale = myLocale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(myLocale);//api 24
            conf.setLayoutDirection(myLocale);//RTL layout
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(conf);
        } else {
            //resources.updateConfiguration(configuration,displayMetrics);
            res.updateConfiguration(conf, dm);
        }

        Locale.setDefault(myLocale);

        System.out.println("-- in changelocale lang  " + lang);

        prefs.edit().putString("lang_key", lang).apply();

        Intent intent = new Intent(this, DashbordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}