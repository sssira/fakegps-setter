package io.github.jqssun.gpssetter.ui

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
        // Disesuaikan jadi activity_map (tanpa s) sesuai isi folder res lo
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
