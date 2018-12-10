package com.conversify.ui.venues.map

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.MapVenue
import com.conversify.data.remote.models.venues.VenueDto
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.layout_map_venue.view.*
import timber.log.Timber

class VenuesMapMarkerRenderer(context: Context,
                              googleMap: GoogleMap,
                              clusterManager: ClusterManager<MapVenue>,
                              private val callback: VenuesMapHelper.Callback) :
        DefaultClusterRenderer<MapVenue>(context.applicationContext, googleMap, clusterManager) {
    private val mapMarkerView = View.inflate(context, R.layout.layout_map_venue, null)
    private val iconGenerator = IconGenerator(context)
    private val textColorNormal = ContextCompat.getColor(context, R.color.colorPrimary)
    private val textColorSelected = ContextCompat.getColor(context, R.color.white)

    // Key - "venueId"
    private val venueMarkersMap = mutableMapOf<String, Marker>()

    private var lastSelectedMapVenue: MapVenue? = null

    init {
        iconGenerator.setContentView(mapMarkerView)
        iconGenerator.setBackground(null)
    }

    override fun onBeforeClusterItemRendered(venue: MapVenue, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(venue, markerOptions)

        // Update the marker before it is rendered
        updateMarkerView(venue)

        markerOptions.flat(true)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
    }

    override fun onClusterItemRendered(clusterItem: MapVenue, marker: Marker) {
        // Add marker to the markers HashMap when it is rendered
        venueMarkersMap[clusterItem.venue.id ?: ""] = marker
        super.onClusterItemRendered(clusterItem, marker)
    }

    private fun updateMarkerView(venue: MapVenue) {
        mapMarkerView.tvMemberCount.text = venue.title

        if (venue.isSelected) {
            mapMarkerView.tvMemberCount.setTextColor(textColorSelected)
            mapMarkerView.ivMapVenueMarker.setImageResource(R.drawable.ic_venue_marker_selected)
        } else {
            mapMarkerView.tvMemberCount.setTextColor(textColorNormal)
            mapMarkerView.ivMapVenueMarker.setImageResource(R.drawable.ic_venue_marker_normal)
        }
    }

    private fun notifyClickCallback(mapVenue: MapVenue) {
        if (mapVenue.isSelected) {
            callback.onMapVenueSelected(mapVenue.venue)
        } else {
            callback.onMapVenueDeselected()
        }
    }

    private fun getVenueMarker(venueId: String?): Marker? {
        venueId ?: return null
        return venueMarkersMap[venueId]
    }

    private fun updateMarkerSelection(mapVenue: MapVenue, isSelected: Boolean) {
        try {
            val selectedMarker = getVenueMarker(mapVenue.venue.id)
            mapVenue.isSelected = isSelected
            updateMarkerView(mapVenue)
            selectedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
        } catch (exception: Exception) {
            Timber.w(exception)
        }
    }

    fun onMarkerClicked(clickedMapVenue: MapVenue) {
        val lastSelectedVenue = lastSelectedMapVenue

        val clickedVenueId = clickedMapVenue.venue.id
        val lastSelectedVenueId = lastSelectedMapVenue?.venue?.id

        // De-select current selected marker if it is different from clicked one
        if (lastSelectedVenue != null && lastSelectedVenueId != clickedVenueId) {
            updateMarkerSelection(lastSelectedVenue, false)
        }

        if (lastSelectedVenueId == null || lastSelectedVenueId != clickedVenueId) {
            // If selected venue is different from clicked then set clicked marker as selected
            updateMarkerSelection(clickedMapVenue, true)
            notifyClickCallback(clickedMapVenue)
        } else {
            // If selected and current clicked are same then toggle the state
            updateMarkerSelection(clickedMapVenue, !clickedMapVenue.isSelected)
            notifyClickCallback(clickedMapVenue)
        }

        lastSelectedMapVenue = clickedMapVenue
    }

    fun clearLastSelection() {
        val lastSelectedVenue = lastSelectedMapVenue
        if (lastSelectedVenue != null) {
            updateMarkerSelection(lastSelectedVenue, false)
        }
        lastSelectedMapVenue = null
    }

    fun clearAllItems() {
        clearLastSelection()
        venueMarkersMap.clear()
    }

    override fun onBeforeClusterRendered(cluster: Cluster<MapVenue>?, markerOptions: MarkerOptions?) {
        val lastSelectedVenue = lastSelectedMapVenue

        /*
        * Before rendering the cluster, first check if the last selected venue is going to be
        * inside the cluster. If true then de-select the venue on the map.
        * */
        if (lastSelectedVenue != null) {
            val clusterItems = cluster?.items ?: emptyList()

            // Find selected venue in cluster
            val selectedItemInCluster = clusterItems.firstOrNull {
                it.venue.id == lastSelectedVenue.venue.id
            } != null

            // If selected venue is in cluster, then de-select it.
            if (selectedItemInCluster) {
                updateMarkerSelection(lastSelectedVenue, false)
                callback.onMapVenueDeselected()
            }
        }
        super.onBeforeClusterRendered(cluster, markerOptions)
    }

    interface Callback {
        fun onMapVenueSelected(venue: VenueDto)
        fun onMapVenueDeselected()
    }
}