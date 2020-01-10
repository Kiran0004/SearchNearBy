package com.fueled.search.nearbyfood.map

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fueled.search.nearbyfood.R
import com.fueled.search.nearbyfood.detail.ItemDetailActivity
import com.fueled.search.nearbyfood.detail.KEY_VENUE_ID
import com.fueled.search.nearbyfood.search.SearchViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds


const val KEY_MAP_DETAILS = "MAP_DETAILS"

/**
 *  Activity which displays all the search results as markers on the Google Map
 */

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private var items: List<SearchViewModel.VenueItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        items = intent?.extras?.getParcelableArrayList(KEY_MAP_DETAILS)






    }




    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnInfoWindowClickListener(this)

        val builder = LatLngBounds.Builder()

        items?.forEach {
            val itemPosition = LatLng(it.lat, it.lng)
            builder.include(itemPosition)
            googleMap.addMarker(MarkerOptions().position(itemPosition).title(it.title))
        }

        // animate camera to the location of the marker with padding from edge
        val bounds = builder.build()
        val padding = 200
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.animateCamera(cameraUpdate)
    }

    override fun onInfoWindowClick(marker: Marker) {
        val id: String? = items?.single { venue -> venue.title == marker.title }?.id
        val intent = Intent(this, ItemDetailActivity::class.java)
        intent.putExtra(KEY_VENUE_ID, id)
        startActivity(intent)
    }
}
