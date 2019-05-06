package org.greenstand.android.TreeTracker.utilities

import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission

/**
 * Created by Jonathan Muller on 5/5/19.
 */
object GpsHelper {

    var locationUpdatesStarted = false
        private set

    fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun requestLocationUpdates(context: Context, locationListener: android.location.LocationListener) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        locationUpdatesStarted = true
    }

    fun removeLocationUpdates(context: Context, locationListener: android.location.LocationListener) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(locationListener)
        locationUpdatesStarted = false
    }
}