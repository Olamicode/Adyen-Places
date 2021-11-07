package com.adyen.android.assignment.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object Utility {
    fun hasInternet(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}