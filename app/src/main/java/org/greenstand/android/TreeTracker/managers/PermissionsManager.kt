package org.greenstand.android.TreeTracker.managers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.greenstand.android.TreeTracker.application.Permissions

/**
 * Created by Jonathan Muller on 5/5/19.
 */
object PermissionsManager {

    fun requestNeededPermissions(activity: Activity): Boolean {
        if (requiresPermissions(activity)) {
            requestNecessaryPermissions(activity)
            return true
        }
        return false
    }

    private fun requiresPermissions(context: Context) : Boolean {
        return ContextCompat.checkSelfPermission(context,
            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestNecessaryPermissions(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION),
            Permissions.NECESSARY_PERMISSIONS)
    }

}