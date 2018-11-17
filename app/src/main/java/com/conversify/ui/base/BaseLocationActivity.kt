package com.conversify.ui.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.extensions.shortToast
import com.conversify.utils.AppConstants
import com.conversify.utils.PermissionUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import permissions.dispatcher.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

@RuntimePermissions
abstract class BaseLocationActivity : BaseActivity() {
    private val updateInterval: Long = TimeUnit.SECONDS.toMillis(15)

    // Provides access to the Fused Location Provider API.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Provides access to the Location Settings API.
    private lateinit var settingsClient: SettingsClient

    // Stores parameters for requests to the FusedLocationProviderApi.
    private lateinit var locationRequest: LocationRequest

    /*
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    // Callback for Location events.
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { location ->
                    Timber.d(location.toString())
                    UserManager.updateLocation(location)
                }
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.interval = updateInterval

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.fastestInterval = updateInterval / 2

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        locationSettingsRequest = builder.build()
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this) {
                    Timber.i("All location settings are satisfied.")
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                }
                .addOnFailureListener(this) { exception ->
                    val statusCode = (exception as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Timber.i("Location settings are not satisfied. Attempting to upgrade location settings")
                            try {
                                /*
                                * Show the dialog by calling startResolutionForResult(),
                                * and check the result in onActivityResult().
                                * */
                                val apiException = exception as ResolvableApiException
                                apiException.startResolutionForResult(this@BaseLocationActivity,
                                        AppConstants.REQ_CODE_CHECK_LOCATION_SETTINGS)
                            } catch (intentException: IntentSender.SendIntentException) {
                                Timber.i("PendingIntent unable to execute request.")
                            }

                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Timber.w("Location settings are inadequate, and cannot be fixed here. Fix in Settings.")
                        }
                    }
                }
    }

    private fun stopLocationUpdates() {
        /*
        * It is a good practice to remove location requests when the activity is in a paused or
        * stopped state. Doing so helps battery performance and is especially
        * recommended in applications that request frequent location updates.
        * */
        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this) {
                    Timber.d("Location updates stopped")
                }
    }

    @SuppressLint("NoCorrespondingNeedsPermission")
    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationalForLocation(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.permission_rationale_location, request)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationPermissionDenied() {
        shortToast(R.string.permission_denied_location)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationNeverAskAgain() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_again_location, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdatesWithPermissionCheck()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_CHECK_LOCATION_SETTINGS -> {
                when (resultCode) {
                    // Permission granted from settings
                    Activity.RESULT_OK -> {
                        startLocationUpdatesWithPermissionCheck()
                    }

                    // User does not want to update setting.
                    Activity.RESULT_CANCELED -> {
                        shortToast(R.string.permission_denied_location)
                    }
                }
            }
        }
    }
}