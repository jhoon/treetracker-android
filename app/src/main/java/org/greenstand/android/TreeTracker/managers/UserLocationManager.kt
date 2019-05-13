package org.greenstand.android.TreeTracker.managers

import android.location.Location

/**
 * This is pretty empty at the moment, but all logic related to the users location should be put here
 * and if it exists somewhere else, it should be moved here
 */
object UserLocationManager {

    var currentLocation: Location? = null

    var currentTreeLocation: Location? = null

    var allowNewTreeOrUpdate = false
}