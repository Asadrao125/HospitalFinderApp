package com.gexton.hospitalfinderapp.tracking_files;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gexton.hospitalfinderapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.gexton.hospitalfinderapp.tracking_files.RuoteTestActivity.JOB_STATE_CHANGED;
import static com.gexton.hospitalfinderapp.tracking_files.RuoteTestActivity.LOCATION_ACQUIRED;

/**
 * Created by Marty on 11/25/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Handler handler;
    ConnectionDetector cd;
    FusedLocationProviderClient mFusedLocationProviderClient;
    public static final int LOCATION_SERVICE_JOB_ID = 111;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    JobParameters jobParameters;
    public static boolean isJobRunning = false;
    GoogleApiClient mGoogleApiClient;
    ArrayList<Location> updatesList = new ArrayList<>();
    String name;
    String MY_PREFS_NAME = "HospitalFinder";

    public static final String ACTION_STOP_JOB = "actionStopJob";

    private BroadcastReceiver stopJobReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP_JOB)) {
                Log.d("unregister", " job stop receiver");
            /*try {
                unregisterReceiver(this); //Unregister receiver to avoid receiver leaks exception
            }catch (Exception e){
                e.printStackTrace();
            }*/
                onJobFinished();
            }
        }
    };

    private void onJobFinished() {
        Log.d("job finish", " called");
        isJobRunning = false;
        stopLocationUpdates();
        jobFinished(jobParameters, false);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        handler = new Handler();
        this.jobParameters = jobParameters;
        /*th = new LocationThread();
        handler.post(th);*/
        buildGoogleApiClient();
        config();
        isJobRunning = true;
        return true;
    }

    private void config() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        cd = new ConnectionDetector(getApplicationContext());
        startLocationUpdates();
        LocalBroadcastManager.getInstance(LocationJobService.this).registerReceiver(stopJobReceiver, new IntentFilter(ACTION_STOP_JOB));
    }

    private void startLocationUpdates() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    Intent i = new Intent(LOCATION_ACQUIRED);
                    i.putExtra("location", location);

                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(i);

                    if (cd.isConnectingToInternet()) { // check whether internet is available or not
                        updatesList.add(location); //if available add latest location point and send list to server
                        //Intent i1 = new Intent(LocationJobService.this, UploadLocationService.class);
                        //i1.putParcelableArrayListExtra("points", updatesList);
                        updatesList.clear();
                    } else { // if there is no internet connection
                        updatesList.add(location); // add location points to the list
                    }
                }
            }

            ;
        };
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_permission_required), Toast.LENGTH_SHORT).show();
            return;
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationJobService.this);
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        getSharedPreferences("track", MODE_PRIVATE).edit().putBoolean("isServiceStarted", true).apply();
        Intent jobStartedMessage = new Intent(JOB_STATE_CHANGED);
        jobStartedMessage.putExtra("isStarted", true);
        Log.d("send broadcast", " as job started");
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(jobStartedMessage);
        createNotification();
        //Toast.makeText(getApplicationContext(), "Location job service started", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), getString(R.string.toast_navigation_stated), Toast.LENGTH_SHORT).show();
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        } else {
            Log.e("api client", "not null");
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("job", "stopped");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        isJobRunning = false;
        stopLocationUpdates();
        /*try {
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(stopJobReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("track.JobService", "google API client connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("track.JobService", "google API client suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("track.JobService", "google API client failed");
    }

    private void createNotification() {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        if (!TextUtils.isEmpty(prefs.getString("name", "NoValue")) && !prefs.getString("name", "NoValue").equals("NoValue")) {
            name = prefs.getString("name", "NoValue");
        } else {
            name = getString(R.string.hospital_finder);
        }

        PendingIntent pI;
        pI = PendingIntent.getActivity(this, 0, new Intent(this, NavigationActivity.class), 0);

        Notification.Builder mBuilder = new Notification.Builder(getBaseContext());
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = mBuilder.setSmallIcon(R.drawable.doctor)
                    .setTicker(getString(R.string.notification_ticker_tracking))
                    .setWhen(0)
                    .setAutoCancel(false)
                    .setCategory(Notification.EXTRA_BIG_TEXT)
                    .setContentTitle(name)
                    //.setContentText("Your trip in progress")
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(ContextCompat.getColor(getBaseContext(), R.color.red))
                    .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.notification_content_title_trip_in_progress)))
                    .setChannelId("track_marty")
                    .setContentIntent(pI)
                    .setShowWhen(true)
                    .setOngoing(true)
                    .build();
        } else {
            notification = mBuilder
                    .setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.drawable.ic_side_menu : R.mipmap.hospital)
                    .setTicker(getString(R.string.notification_ticker_tracking))
                    .setWhen(0)
                    .setAutoCancel(false)
                    .setCategory(Notification.EXTRA_BIG_TEXT)
                    .setContentTitle(name)
                    //.setContentText("Track in progress")
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(ContextCompat.getColor(getBaseContext(), R.color.black))
                    .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.notification_content_title_trip_in_progress)))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setShowWhen(true)
                    .setContentIntent(pI)
                    .setOngoing(true)
                    .build();
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("track_marty", "Track", NotificationManager.IMPORTANCE_HIGH);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        /*assert notificationManager != null;
        notificationManager.notify(0, notification);*/
        startForeground(1, notification); //for foreground service, don't use 0 as id. it will not work.
    }

    private void removeNotification() {
        /*NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(0);*/ //use this for normal service
        stopForeground(true); // use this for foreground service
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        Log.d("stop location ", " updates called");
        if (mLocationCallback != null && mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            //Toast.makeText(getApplicationContext(), "Location job service stopped.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), getString(R.string.toast_navigation_stopped), Toast.LENGTH_SHORT).show();
        }
        getSharedPreferences("track", MODE_PRIVATE).edit().putBoolean("isServiceStarted", false).apply();
        Intent jobStoppedMessage = new Intent(JOB_STATE_CHANGED);
        jobStoppedMessage.putExtra("isStarted", false);
        Log.d("broadcasted", "job state change");
        removeNotification();
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(jobStoppedMessage);
    }
}