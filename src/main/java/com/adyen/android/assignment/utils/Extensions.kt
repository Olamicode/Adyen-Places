package com.adyen.android.assignment.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.adyen.android.assignment.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar


fun Activity.checkForLocationPermission(
    name: String, requestCode: Int,
    action: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            action.invoke()
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showRequestPermissionRationaleDialog(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                name,
                requestCode
            )
        }
    } else {
        requestForPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
    }
}


fun Activity.requestForPermission(permission: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(
        this,
        permission,
        requestCode
    )
}


fun Activity.showRequestPermissionRationaleDialog(
    permission: Array<String>,
    name: String,
    requestCode: Int
) {

    AlertDialog.Builder(this)
        .apply {
            setMessage(getString(R.string.permission_to_access, name))
            setTitle(getString(R.string.permission_required_title))
            setPositiveButton(getString(R.string.okay)) { _, _ ->
                requestForPermission(permission, requestCode)
            }
        }.create()
        .show()
}

fun Activity.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun Activity.enableGPS(locationRequest: LocationRequest, requestCode: Int) {

    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    builder.setAlwaysShow(true)

    val result: Task<LocationSettingsResponse> =
        LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())

    result.addOnCompleteListener { task ->
        try {
            val response: LocationSettingsResponse = task.getResult(ApiException::class.java)

        } catch (exception: ApiException) {
            if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                val resolvable = exception as ResolvableApiException
                resolvable.startResolutionForResult(
                    this,
                    requestCode
                )
            }
        }
    }

}

fun View.hideView(){
    visibility = View.GONE
}

fun View.showView(){
    visibility = View.VISIBLE
}

fun flattenListToString(stringList: List<String>): String{
    return stringList.joinToString(", ")
}

fun View.showSnackBar(snackBarText: String, timeLength: Int) {
    Snackbar.make(this, snackBarText, timeLength).run {
        show()
    }
}