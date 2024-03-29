package com.gexton.hospitalfinderapp.extras

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.gexton.hospitalfinderapp.BuildConfig
import com.gexton.hospitalfinderapp.R
import com.gexton.hospitalfinderapp.gps.GPSTracker
import com.gexton.hospitalfinderapp.tracking_files.NavigationActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import kotlin.time.ExperimentalTime

class RuoteAndTrackActivity : AppCompatActivity(), OnMapReadyCallback {

    var name = ""
    var address = ""
    var hLat = 0.0
    var hLong = 0.0
    var cLatitude = 0.0
    var cLongitude = 0.0
    val serverKey = "AIzaSyBx_ZNPy1AlHfpip8-Pcyci76Rb6IkkON8"
    private lateinit var mMap: GoogleMap
    lateinit var tv: TextView
    lateinit var btnNavigation: Button
    var newDistance = 0.0
    var newDuration = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruote_and_track)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getCurrentLocation()

        name = intent.getStringExtra("name").toString()
        address = intent.getStringExtra("address").toString()
        hLat = intent.getDoubleExtra("lat", 1000.0)
        hLong = intent.getDoubleExtra("lng", 1000.0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.black, this.theme)
        }

        supportActionBar?.title = name
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        tv = findViewById(R.id.tvDistance) as TextView
        btnNavigation = findViewById(R.id.btnNavigation) as Button

        btnNavigation.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("address", address)
            intent.putExtra("hLat", hLat)
            intent.putExtra("hLong", hLong)
            intent.putExtra("cLat", cLatitude)
            intent.putExtra("cLong", cLongitude)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        requestDirection()
    }

    @OptIn(ExperimentalTime::class)
    private fun requestDirection() {
        GoogleDirectionConfiguration.getInstance().isLogEnabled = BuildConfig.DEBUG
        GoogleDirection.withServerKey(serverKey)
                .from(LatLng(cLatitude, cLongitude))
                .to(LatLng(hLat, hLong))
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(true)
                .execute(
                        onDirectionSuccess = { direction: Direction? ->
                            if (direction != null) {
                                if (direction.isOK()) {

                                    direction.routeList.get(0).totalDistance
                                    direction.routeList.get(0).totalDuration

                                    newDistance = (direction.routeList.get(0).totalDistance / 1000).toDouble()
                                    newDuration = (direction.routeList.get(0).totalDuration / 60).toDouble()

                                    tv.text = "Distance: " + newDistance + " km" + "\nDuration: " + newDuration + " mins"
                                    val route = direction.routeList[0]

                                    //Marker for current location
                                    mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
                                            .title("My Location")
                                            .position(LatLng(cLatitude, cLongitude)))

                                    //MArker for hospital location
                                    mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.hospital))
                                            .title(name)
                                            .snippet(address)
                                            .position(LatLng(hLat, hLong)))

                                    val rnd = Random()
                                    val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

                                    val directionPositionList = route.legList[0].directionPoint
                                    mMap?.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, color))
                                    setCameraWithCoordinationBounds(route)
                                } else {
                                    println("Cannot Draw Route")
                                }
                            }
                        },
                        onDirectionFailure = { t: Throwable ->
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
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                //gps.showSettingsAlert()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}