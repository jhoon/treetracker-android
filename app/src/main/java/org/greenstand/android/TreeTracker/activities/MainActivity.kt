package org.greenstand.android.TreeTracker.activities


import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_new_tree.*
import kotlinx.android.synthetic.main.fragment_tree_preview.*
import org.greenstand.android.TreeTracker.R
import org.greenstand.android.TreeTracker.dialogs.showNoGpsDialog
import org.greenstand.android.TreeTracker.fragments.AboutFragment
import org.greenstand.android.TreeTracker.fragments.DataFragment
import org.greenstand.android.TreeTracker.fragments.LoginFragment
import org.greenstand.android.TreeTracker.fragments.MapsFragment
import org.greenstand.android.TreeTracker.managers.PermissionsManager
import org.greenstand.android.TreeTracker.managers.UserLocationManager
import org.greenstand.android.TreeTracker.managers.UserLocationManager.currentLocation
import org.greenstand.android.TreeTracker.managers.UserLocationManager.currentTreeLocation
import org.greenstand.android.TreeTracker.utilities.GpsHelper
import org.greenstand.android.TreeTracker.utilities.ValueHelper
import timber.log.Timber

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    MapsFragment.LocationDialogListener {

    private var mSharedPreferences: SharedPreferences? = null

    private var fragment: Fragment? = null

    private var fragmentTransaction: FragmentTransaction? = null

    private var locationListener: android.location.LocationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            this@MainActivity.onLocationChanged(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). **Note: Otherwise it is null.**
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSharedPreferences = this.getSharedPreferences(
                "org.greenstand.android", Context.MODE_PRIVATE)


        if (mSharedPreferences!!.getBoolean(ValueHelper.FIRST_RUN, true)) {

            if (mSharedPreferences!!.getBoolean(ValueHelper.TREE_TRACKER_SETTINGS_USED, true)) {
                mSharedPreferences?.edit()?.putBoolean(ValueHelper.TREE_TRACKER_SETTINGS_USED, true)?.apply()
            }

            mSharedPreferences?.edit()?.putBoolean(ValueHelper.FIRST_RUN, false)?.apply()
        }

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""


        val extras = intent.extras
        var startDataSync = false
        if (extras != null) {
            if (extras.getBoolean(ValueHelper.RUN_FROM_NOTIFICATION_SYNC)) {
                startDataSync = true
            }
        }

        if (startDataSync) {
            Timber.d("MainActivity startDataSync is true")
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(ValueHelper.WIFI_NOTIFICATION_ID)

            val dataFragment = DataFragment()
            dataFragment.arguments = intent.extras

            val fragmentTransaction = supportFragmentManager
                    .beginTransaction()
            fragmentTransaction.add(R.id.containerFragment, dataFragment, ValueHelper.DATA_FRAGMENT)

            fragmentTransaction.commit()

        }

        val userIdentifier = mSharedPreferences?.getString(ValueHelper.PLANTER_IDENTIFIER, null)
        if (userIdentifier == null) {
            updatePreferences()

            toolbarTitle.text = resources.getString(R.string.user_not_identified)

            fragment = LoginFragment()
            fragmentTransaction = supportFragmentManager
                .beginTransaction()
            fragmentTransaction?.replace(R.id.containerFragment, fragment as LoginFragment)
                ?.addToBackStack(ValueHelper.IDENTIFY_FRAGMENT)?.commit()

        } else if (mSharedPreferences!!.getBoolean(ValueHelper.TREES_TO_BE_DOWNLOADED_FIRST, false)) {
            Timber.d("TREES_TO_BE_DOWNLOADED_FIRST is true")
            var bundle = intent.extras

            fragment = MapsFragment()
            fragment?.arguments = bundle

            fragmentTransaction = supportFragmentManager
                    .beginTransaction()
            fragmentTransaction?.replace(R.id.containerFragment, fragment as MapsFragment)
                ?.addToBackStack(ValueHelper.MAP_FRAGMENT)?.commit()

            if (bundle == null)
                bundle = Bundle()

            bundle.putBoolean(ValueHelper.RUN_FROM_HOME_ON_LOGIN, true)


            fragment = DataFragment()
            fragment?.arguments = bundle

            fragmentTransaction = supportFragmentManager
                    .beginTransaction()
            fragmentTransaction?.replace(R.id.containerFragment, fragment as DataFragment)
                ?.addToBackStack(ValueHelper.DATA_FRAGMENT)?.commit()

        } else {
            if (userIdentifier != getString(R.string.user_not_identified)) {
                Timber.d("MainActivity" + " startDataSync is false")
                val homeFragment = MapsFragment()
                homeFragment.arguments = intent.extras

                val fragmentTransaction = supportFragmentManager
                        .beginTransaction()
                fragmentTransaction.replace(R.id.containerFragment, homeFragment).addToBackStack(ValueHelper.MAP_FRAGMENT)
                fragmentTransaction.commit()
            } else {
                updatePreferences()

                toolbarTitle.text = resources.getString(R.string.user_not_identified)

                fragment = LoginFragment()
                fragmentTransaction = supportFragmentManager
                        .beginTransaction()
                fragmentTransaction?.replace(R.id.containerFragment,
                        fragment as LoginFragment)?.addToBackStack(ValueHelper.IDENTIFY_FRAGMENT)?.commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val bundle: Bundle?
        val fm = supportFragmentManager
        when (item.itemId) {
            android.R.id.home -> {

                if (fm.backStackEntryCount > 0) {
                    fm.popBackStack()
                }
                return true
            }
            R.id.action_data -> {
                fragment = DataFragment()
                bundle = intent.extras
                fragment?.arguments = bundle

                fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction?.replace(R.id.containerFragment, fragment as DataFragment)
                    ?.addToBackStack(ValueHelper.DATA_FRAGMENT)?.commit()
                for (entry in 0 until fm.backStackEntryCount) {
                    Timber.d("MainActivity " + "Found fragment: " + fm.getBackStackEntryAt(entry).name)
                }
                return true
            }
            R.id.action_about -> {
                val someFragment = supportFragmentManager.findFragmentById(R.id.containerFragment)

                var aboutIsRunning = false

                if (someFragment != null) {
                    if (someFragment is AboutFragment) {
                        aboutIsRunning = true
                    }
                }

                if (!aboutIsRunning) {
                    fragment = AboutFragment()
                    fragment?.arguments = intent.extras

                    fragmentTransaction = supportFragmentManager
                            .beginTransaction()
                    fragmentTransaction?.replace(R.id.containerFragment, fragment as AboutFragment)
                        ?.addToBackStack(ValueHelper.ABOUT_FRAGMENT)?.commit()
                }
                for (entry in 0 until fm.backStackEntryCount) {
                    Timber.d("MainActivity " + "Found fragment: " + fm.getBackStackEntryAt(entry).name)
                }
                return true
            }

            R.id.action_change_user -> {
                updatePreferences()

                toolbarTitle.text = resources.getString(R.string.user_not_identified)

                fragment = LoginFragment()
                fragmentTransaction = supportFragmentManager
                        .beginTransaction()
                fragmentTransaction?.replace(R.id.containerFragment, fragment as LoginFragment)
                    ?.addToBackStack(ValueHelper.IDENTIFY_FRAGMENT)?.commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    public override fun onPause() {
        super.onPause()

        stopPeriodicUpdates()
    }

    /*
     * Called when the system detects that this Activity is now visible.
     */
    public override fun onResume() {
        super.onResume()

        if (PermissionsManager.requestNeededPermissions(this)) {
            startPeriodicUpdates()
        }
    }

    fun onLocationChanged(location: Location) {
        //Timber.d("onLocationChanged", location.toString());

        // In the UI, set the latitude and longitude to the value received
        currentLocation = location

        //int minAccuracy = mSharedPreferences.getInt(ValueHelper.MIN_ACCURACY_GLOBAL_SETTING, 0);
        val minAccuracy = 10


        if (fragmentMapGpsAccuracy != null) {
            if (currentLocation != null) {
                if (currentLocation!!.hasAccuracy() && currentLocation!!.accuracy < minAccuracy) {
                    fragmentMapGpsAccuracy.setTextColor(Color.GREEN)
                    fragmentMapGpsAccuracyValue?.setTextColor(Color.GREEN)
                    fragmentMapGpsAccuracyValue?.text = Integer.toString(
                        Math.round(currentLocation!!.accuracy)) + " " + resources.getString(R.string.meters)
                    UserLocationManager.allowNewTreeOrUpdate = true
                } else {
                    fragmentMapGpsAccuracy.setTextColor(Color.RED)
                    UserLocationManager.allowNewTreeOrUpdate = false

                    if (currentLocation!!.hasAccuracy()) {
                        fragmentMapGpsAccuracyValue?.setTextColor(Color.RED)
                        fragmentMapGpsAccuracyValue?.text = Integer.toString(
                            Math.round(currentLocation!!.accuracy)) + " " + resources.getString(R.string.meters)
                    } else {
                        fragmentMapGpsAccuracyValue?.setTextColor(Color.RED)
                        fragmentMapGpsAccuracyValue?.text = "N/A"
                    }
                }

                if (currentLocation!!.hasAccuracy()) {
                    if (fragmentNewTreeGpsAccuracy != null) {
                        fragmentNewTreeGpsAccuracy?.text = Integer.toString(
                            Math.round(currentLocation!!.accuracy)) + " " + resources.getString(R.string.meters)
                    }
                }
            } else {
                fragmentMapGpsAccuracy.setTextColor(Color.RED)
                fragmentMapGpsAccuracyValue?.setTextColor(Color.RED)
                fragmentMapGpsAccuracyValue?.text = "N/A"
                UserLocationManager.allowNewTreeOrUpdate = false
            }


            if (currentTreeLocation != null && UserLocationManager.currentLocation != null) {
                val results = floatArrayOf(0f, 0f, 0f)
                Location.distanceBetween(UserLocationManager.currentLocation!!.latitude, UserLocationManager.currentLocation!!.longitude,
                    UserLocationManager.currentTreeLocation!!.latitude, UserLocationManager.currentTreeLocation!!.longitude, results)

                if (fragmentNewTreeDistance != null) {
                    fragmentNewTreeDistance.text = Integer.toString(Math.round(results[0])) + " " + resources.getString(R.string.meters)
                }

                if (fragmentTreePreviewDistance != null) {
                    fragmentTreePreviewDistance.text = Integer.toString(Math.round(results[0])) + " " + resources.getString(R.string.meters)
                }
            }
        } else {
            Timber.d("fragmentMapGpsAccuracy NULL" );
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionsManager.checkGrantedPermissions(requestCode, grantResults)) {
            startPeriodicUpdates()
        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    @SuppressLint("MissingPermission")
    private fun startPeriodicUpdates() {
        if (PermissionsManager.requestNeededPermissions(this)) {
            Toast.makeText(this, "GPS Permissions Not Enabled", Toast.LENGTH_LONG).show()
            return
        }

        // TODO this check may not longer be necessary
        if (!GpsHelper.isGPSEnabled(this)) {
            showNoGpsDialog(this)
            return
        }

        if (GpsHelper.locationUpdatesStarted) {
            return
        }

        // Register the listener with Location Manager's network provider
        GpsHelper.requestLocationUpdates(this, locationListener)
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private fun stopPeriodicUpdates() {
        GpsHelper.removeLocationUpdates(this, locationListener)
    }

    private fun updatePreferences() {
        val editor = mSharedPreferences?.edit()
        editor?.putLong(ValueHelper.TIME_OF_LAST_USER_IDENTIFICATION, 0)
        editor?.putString(ValueHelper.PLANTER_IDENTIFIER, null)
        editor?.putString(ValueHelper.PLANTER_PHOTO, null)
        editor?.apply()
    }

    override fun refreshMap() {
        startPeriodicUpdates()
    }
}

