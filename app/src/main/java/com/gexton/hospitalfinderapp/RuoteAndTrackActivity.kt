package com.gexton.hospitalfinderapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.gexton.hospitalfinderapp.gps.GPSTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class RuoteAndTrackActivity : AppCompatActivity(), OnMapReadyCallback {

    var name = ""
    var address = ""
    var hLat = 0.0
    var hLong = 0.0
    var cLatitude = 0.0;
    var cLongitude = 0.0;
    val serverKey = "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8"
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruote_and_track)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getCurrentLocation()

        name = intent.getStringExtra("name").toString()
        address = intent.getStringExtra("address").toString()
        hLat = intent.getDoubleExtra("lat", 1000.0)
        hLong = intent.getDoubleExtra("lng", 1000.0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.red, this.theme)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        requestDirection()
    }

    private fun requestDirection() {

        GoogleDirectionConfiguration.getInstance().isLogEnabled = BuildConfig.DEBUG
        GoogleDirection.withServerKey(serverKey)
                .from(LatLng(cLatitude, cLongitude))
                .to(LatLng(hLat, hLong))
                .transportMode(TransportMode.DRIVING)
                .execute(
                        onDirectionSuccess = { direction: Direction? ->
                            if (direction != null) {
                                if (direction.isOK()) {
                                    // Do something
                                    val route = direction.routeList[0]
                                    //Marker for current location
                                    mMap?.addMarker(MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
                                            .title("My Location")
                                            .position(LatLng(cLatitude, cLongitude)))

                                    //MArker for hospital location
                                    mMap?.addMarker(MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.hospital))
                                            .title(name)
                                            .snippet(address)
                                            .position(LatLng(hLat, hLong)))

                                    val directionPositionList = route.legList[0].directionPoint
                                    mMap?.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED))
                                    setCameraWithCoordinationBounds(route)
                                } else {
                                    // Do something
                                    println("Cannot Draw Route")
                                }
                            }
                        },
                        onDirectionFailure = { t: Throwable ->
                            // Do something
                        }
                )
    }

    private fun setCameraWithCoordinationBounds(route: Route) {
        val southwest = route.bound.southwestCoordination.coordination
        val northeast = route.bound.northeastCoordination.coordination
        val bounds = LatLngBounds(southwest, northeast)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((applicationContext as Activity), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            val gps = GPSTracker(applicationContext)
            if (gps.canGetLocation()) {
                cLatitude = gps.latitude
                cLongitude = gps.longitude
            } else {
                gps.showSettingsAlert()
            }
        }
    }

}