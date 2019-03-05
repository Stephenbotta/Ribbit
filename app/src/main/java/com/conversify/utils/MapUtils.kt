package com.conversify.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.conversify.R
import timber.log.Timber

object MapUtils {
    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
        val navigationUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val navigationIntent = Intent(Intent.ACTION_VIEW, navigationUri)
        navigationIntent.`package` = "com.google.android.apps.maps"
        if (navigationIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(navigationIntent)
        } else {
            AlertDialog.Builder(context)
                    .setMessage(R.string.message_google_maps_is_not_installed)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        try {
                            val googleMapsStoreUri = Uri.parse("market://details?id=com.google.android.apps.maps")
                            val googleMapsStoreIntent = Intent(Intent.ACTION_VIEW, googleMapsStoreUri)
                            context.startActivity(googleMapsStoreIntent)
                        } catch (exception: Exception) {
                            Timber.e(exception)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()
        }
    }

    fun getStaticMapWithMarker(context: Context, latitude: Double, longitude: Double): String {
        val key = context.getString(R.string.app_google_api_key)
        return "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=600x350&scale=1&markers=$latitude,$longitude&key=$key"
    }
}