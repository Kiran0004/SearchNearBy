package com.fueled.search.nearbyfood.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fueled.search.nearbyfood.R
import com.fueled.search.nearbyfood.detail.ItemDetailActivity
import com.fueled.search.nearbyfood.detail.KEY_VENUE_ID
import com.fueled.search.nearbyfood.map.KEY_MAP_DETAILS
import com.fueled.search.nearbyfood.map.MapViewActivity
import com.fueled.search.nearbyfood.util.distinct

import kotlinx.android.synthetic.main.activity_search.*
import org.koin.androidx.viewmodel.ext.android.viewModel

import com.google.android.gms.common.api.GoogleApiClient
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

/**
 * Created by Kiran.
 */



class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val searchItems = mutableListOf<SearchViewModel.VenueItem>()

    private lateinit var adapter: RecyclerAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val searchViewModel: SearchViewModel by viewModel()

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null

    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var locationManager: LocationManager? = null

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
        searchView.setOnQueryTextListener(this)
        searchViewModel.performSearch("restaurant")
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        searchViewModel.progressLiveData.observe(this, Observer {
            progressView.visibility = if (it == true) View.VISIBLE else View.GONE
            if (it == true) ivSearchHolder.visibility = View.GONE
        })

        searchViewModel.errorLiveData.observe(this, Observer {
            showError(it)
        })

        searchViewModel
            .venuesLiveData
            .distinct()
            .observe(this, Observer<List<SearchViewModel.VenueItem>> {
                searchItems.clear()
                searchItems.addAll(it)
                adapter.notifyDataSetChanged()
                if (it.isEmpty()) ivSearchHolder.visibility = View.VISIBLE
            })

        linearLayoutManager = LinearLayoutManager(this)
        rvSearchContent.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(
            searchItems, object : ClickHandler {
                override fun onSearchItemClick(item: SearchViewModel.VenueItem) {
                    val intent = Intent(this@SearchActivity, ItemDetailActivity::class.java)
                    intent.putExtra(KEY_VENUE_ID, item.id)
                    this@SearchActivity.startActivity(intent)
                    overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up )
                }

                override fun onStarClick(item: SearchViewModel.VenueItem, selected: Boolean) {
                    if (selected) {

                        searchViewModel.removeFromFavorites(item.id)
                    } else {
                        searchViewModel.addToFavorites(item.id)
                    }
                }
            })
        rvSearchContent.adapter = adapter
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        fabMap.setOnClickListener { view ->

            searchViewModel.onMapViewClick(object : SearchViewModel.MapClickHandler {

                override fun showMap(list: List<SearchViewModel.VenueItem>) {
                    val arrayList = ArrayList<SearchViewModel.VenueItem>()
                    arrayList.addAll(list)
                    val intent = Intent(view.context, MapViewActivity::class.java)
                    intent.putParcelableArrayListExtra(KEY_MAP_DETAILS, arrayList)
                    view.context.startActivity(intent)
                }

                override fun showError(msg: String) {
                    Toast.makeText(view.context, msg, Toast.LENGTH_LONG).show()
                }
            })
        }
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        performSearch(newText)
        return false
    }

    private fun performSearch(query: String) {
        searchViewModel.performSearch(query)
    }

    private fun showError(errorMsg: String) {
        Snackbar.make(root, errorMsg, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                searchViewModel.performSearch(searchView.query.toString())
            }.show()
    }
    //////////////////////////

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        SearchViewModel.LOCATION_DTL_LAT = location.latitude
                        SearchViewModel.LOCATION_DTL_LNG = location.longitude
                        //findViewById<TextView>(R.id.latTextView).text = location.latitude.toString()
                        //findViewById<TextView>(R.id.lonTextView).text = location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

           // findViewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
            //findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}
