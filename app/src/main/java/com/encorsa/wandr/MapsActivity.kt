package com.encorsa.wandr

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var objective: ObjectiveDatabaseModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        objective = intent.getParcelableExtra<ObjectiveDatabaseModel>("objective")
        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
      //  val location = LatLng(-34.0, 151.0)
        objective?.let {
            val location = LatLng(it.latitude, it.longitude)
            val marker = mMap.addMarker(MarkerOptions().position(location).title(it.name).snippet(it.address))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.0f))
            marker.showInfoWindow()
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

    }
}
