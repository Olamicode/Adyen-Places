package com.adyen.android.assignment.ui


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.model.RecommendedItem
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.adyen.android.assignment.repository.PlacesRepository
import com.adyen.android.assignment.ui.adapter.VenueAdapter
import com.adyen.android.assignment.utils.*
import com.adyen.android.assignment.utils.callbacks.ObjectCallback
import com.adyen.android.assignment.utils.callbacks.OfflineCallBack
import com.adyen.android.assignment.viewmodel.PlacesViewModel
import com.adyen.android.assignment.viewmodel.PlacesViewModelFactory
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity_search.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    private lateinit var placesViewModel: PlacesViewModel
    private  var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var venueAdapter: VenueAdapter
    private var isLocationResultFirstLoadedSuccessfully: Boolean = false

    companion object {
        private const val REQUEST_CODE = 101
        private const val REQUEST_CHECK_SETTINGS = 10001
    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (isGpsEnabled) {
                    getLocationResults()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_search)

        val placesRepository = PlacesRepository(PlacesService.instance)
        val placesViewModelFactory = PlacesViewModelFactory(placesRepository)
        placesViewModel =
            ViewModelProvider(this, placesViewModelFactory).get(PlacesViewModel::class.java)


        venueAdapter = VenueAdapter()

        search_results_rv.apply {
            adapter = venueAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        swipe_refresh_layout.setOnRefreshListener {
            getLocationResults()
        }

        retry_btn.setOnClickListener {
            progress_bar_circular.showView()
            getLocationResults()
        }

        getLocationResults()

    }

    override fun onResume() {
        super.onResume()

        IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(Intent.ACTION_PROVIDER_CHANGED)
            registerReceiver(broadcastReceiver, this)
        }
    }

    private fun getLocationResults() {
        getLocationResults(
            object : ObjectCallback<LocationResult> {
                override fun onCallback(obj: LocationResult) {
                    val longitude = obj.lastLocation.longitude
                    val latitude = obj.lastLocation.latitude

                    if (latitude != null && longitude != null) {
                        getVenueRecommendations(latitude, longitude)
                    } else {
                        swipe_refresh_layout.isRefreshing = false
                    }
                }
            }, object : ObjectCallback<Exception> {
                override fun onCallback(obj: Exception) {
                    main_activity.showSnackBar(obj.localizedMessage, Snackbar.LENGTH_SHORT)
                }
            }
        )
    }

    private fun initializeFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    private fun initializeLocationRequest(): LocationRequest = LocationRequest()


    private fun getLocationResults(
        locationResultCallback: ObjectCallback<LocationResult>,
        errorCallback: ObjectCallback<Exception>
    ) {

        if (isLocationEnabled()) {
            subscribeToLocationUpdates(locationResultCallback, errorCallback)
        } else {
               handleErrorViews(getString(R.string.could_not_load_search_results,
                   getString(R.string.location_error)),
                   isLocationResultFirstLoadedSuccessfully
               )
            val locationRequest = initializeLocationRequest()
            enableGPS(locationRequest, REQUEST_CHECK_SETTINGS)
        }

    }


    private fun subscribeToLocationUpdates(
        locationResultCallback: ObjectCallback<LocationResult>,
        errorCallback: ObjectCallback<Exception>
    ) {

        checkForLocationPermission(getString(R.string.location), REQUEST_CODE) {
            initializeFusedLocationClient()
            locationRequest = initializeLocationRequest()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    try {
                        locationResultCallback.onCallback(locationResult)
                    } catch (exception: Exception) {
                        errorCallback.onCallback(exception)
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    private fun getVenueRecommendations(
        latitude: Double,
        longitude: Double
    ) {

        placesViewModel.getVenueRecommendations(this,
            object :
                ObjectCallback<VenueRecommendationsResponse> {
                override fun onCallback(obj: VenueRecommendationsResponse) {
                    progress_bar_circular.hideView()

                    val recommendedItems = arrayListOf<RecommendedItem>()

                    obj.groups.forEach {
                        it.items.forEach { recommendedItem ->
                            recommendedItems.add(recommendedItem)
                        }
                    }

                    swipe_refresh_layout.isRefreshing = false
                    swipe_refresh_layout.isVisible = recommendedItems.isNotEmpty()
                    error_layout.isVisible = recommendedItems.isEmpty()

                    isLocationResultFirstLoadedSuccessfully = true
                    venueAdapter.submitRecommendedItemList(recommendedItems)

                }

            }, object : ObjectCallback<Throwable> {
                override fun onCallback(obj: Throwable) {

                    if (!obj.localizedMessage.isNullOrEmpty()) {
                        handleErrorViews(obj.localizedMessage!!, isLocationResultFirstLoadedSuccessfully)
                    }
                }

            }, object : OfflineCallBack {
                override fun onCallback(message: String) {

                    handleErrorViews(message, isLocationResultFirstLoadedSuccessfully)
                }

            },
            latitude,
            longitude
        )
        swipe_refresh_layout.isRefreshing = false
    }

    private fun handleErrorViews(message: String, isLocationResultFirstLoadedSuccessfully: Boolean) {

        if (!isLocationResultFirstLoadedSuccessfully) {
            swipe_refresh_layout.isRefreshing = false
            progress_bar_circular.hideView()
            retry_tv.text =  message
            swipe_refresh_layout.isVisible = message.isEmpty()
            error_layout.isVisible = message.isNotEmpty()
            retry_btn.isVisible = message.isNotEmpty()
        } else {
            main_activity.showSnackBar(message, Snackbar.LENGTH_SHORT)
        }
    }

    private fun unsubscribeToLocationUpdates() {
        fusedLocationClient.let { fusedLocationClient ->
            val removeTask = fusedLocationClient?.removeLocationUpdates(locationCallback)

            removeTask?.addOnCompleteListener {
                if (it.isSuccessful) {
                    onStop()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_venue_search, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                venueAdapter.filter.filter(newText)
                return false
            }

        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_refresh -> {
                getLocationResults()
                swipe_refresh_layout.isRefreshing = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
        unsubscribeToLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE ->  when {
                grantResults.isEmpty() -> {
                    main_activity.showSnackBar("Location not granted",
                        Snackbar.LENGTH_SHORT)
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    getLocationResults()
                }
                else -> {
                    showRequestPermissionRationaleDialog(
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        getString(R.string.location),
                        requestCode
                    )
                }
            }
        }
    }

}



