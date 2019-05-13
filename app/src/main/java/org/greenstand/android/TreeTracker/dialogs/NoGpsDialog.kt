package org.greenstand.android.TreeTracker.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import org.greenstand.android.TreeTracker.R

/**
 * Created by Jonathan Muller on 5/5/19.
 */
fun showNoGpsDialog(activity: Activity) {

    val dialog = AlertDialog.Builder(activity)
        .setTitle(R.string.enable_location_access)
        .setMessage(R.string.you_must_enable_location_access_in_your_settings_in_order_to_continue)
        .setCancelable(false)
        .setPositiveButton(R.string.ok) { dialog, _ ->
            if (Build.VERSION.SDK_INT >= 19) {
                //LOCATION_MODE
                //Sollution for problem 25 added the ability to pop up location start activity
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                //LOCATION_PROVIDERS_ALLOWED

                val locationProviders = Settings.Secure.getString(activity.contentResolver,
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
                if (locationProviders == null || locationProviders == "") {
                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
            dialog.dismiss()
        }
        .setNegativeButton(R.string.cancel) { dialog, which ->
            activity.finish()
            dialog.dismiss()
        }
        .create()

    dialog.setCanceledOnTouchOutside(false)
    dialog.show()

}