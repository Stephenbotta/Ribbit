package com.conversify.ui.venues.map

import android.content.Context
import com.conversify.data.local.models.MapVenue
import com.conversify.data.remote.models.venues.VenueDto
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager

class VenuesMapHelper(context: Context,
                      private val googleMap: GoogleMap,
                      private val callback: Callback) {
    private val clusterManager = ClusterManager<MapVenue>(context, googleMap)
    private val clusterRenderer = VenuesMapMarkerRenderer(context, googleMap, clusterManager, callback)
    private val mapVenues = mutableListOf<MapVenue>()

    init {
        // Disable all unused settings
        googleMap.uiSettings.apply {
            isCompassEnabled = false
            isRotateGesturesEnabled = false
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = false
            isTiltGesturesEnabled = false
            isIndoorLevelPickerEnabled = false
        }

        googleMap.setOnMapLoadedCallback {
            callback.onMapLoaded(googleMap)

            clusterManager.renderer = clusterRenderer
            clusterManager.setOnClusterItemClickListener(propertyClickListener)

            // Set map listeners
            googleMap.setOnCameraIdleListener(clusterManager)
            googleMap.setOnMarkerClickListener(clusterManager)
            googleMap.setOnInfoWindowClickListener(clusterManager)
            googleMap.setOnMapClickListener {
                clusterRenderer.clearLastSelection()
                callback.onMapClicked()
            }
        }
    }

    private val propertyClickListener = ClusterManager.OnClusterItemClickListener<MapVenue> { mapVenue ->
        clusterRenderer.onMarkerClicked(mapVenue)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapVenue.position, 16f))
        return@OnClusterItemClickListener true
    }

    fun displayVenues(venues: List<VenueDto>) {
        clusterRenderer.clearAllItems()

        if (venues.isEmpty()) {
            mapVenues.clear()
            clusterManager.clearItems()
            clusterManager.cluster()
            return
        }

        val boundsBuilder = LatLngBounds.Builder()

        // Clear any existing listVenues on the map
        mapVenues.clear()

        venues.forEach { venue ->
            val mapVenue = MapVenue(venue)
            mapVenues.add(mapVenue)

            // Include venue location in bounds builder
            boundsBuilder.include(mapVenue.position)
        }

        clusterManager.clearItems()
        clusterManager.addItems(mapVenues)
        clusterManager.cluster()

        // Move camera to bounds when map is loaded
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80))
    }

    fun clear() {
        clusterManager.clearItems()
        googleMap.clear()
        googleMap.setOnMarkerClickListener(null)
    }

    interface Callback : VenuesMapMarkerRenderer.Callback {
        fun onMapLoaded(googleMap: GoogleMap)
        fun onMapClicked()
    }
}