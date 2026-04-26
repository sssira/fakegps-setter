package io.github.jqssun.gpssetter.ui

import io.github.jqssun.gpssetter.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import io.github.jqssun.gpssetter.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseMapActivity : AppCompatActivity(), OnMapReadyCallback {

    protected var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
