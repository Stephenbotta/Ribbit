package com.conversify.ui.venues.map

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.MapVenue
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.layout_map_venue.view.*
import timber.log.Timber

class VenuesMapMarkerRenderer(context: Context,
                              googleMap: GoogleMap,
                              private val clusterManager: ClusterManager<MapVenue>,
                              private val callback: VenuesMapHelper.Callback) :
        DefaultClusterRenderer<MapVenue>(context.applicationContext, googleMap, clusterManager) {
    private val mapMarkerView = View.inflate(context, R.layout.layout_map_venue, null)
    private val iconGenerator = IconGenerator(context)
    private val textColorNormal = ContextCompat.getColor(context, R.color.colorPrimary)
    private val textColorSelected = ContextCompat.getColor(context, R.color.white)

    private var selectedMapVenue: MapVenue? = null

    init {
        iconGenerator.setContentView(mapMarkerView)
    }

    override fun onBeforeClusterItemRendered(venue: MapVenue, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(venue, markerOptions)

        updateMarkerView(venue)
        iconGenerator.setBackground(null)

        markerOptions.flat(true)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
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

    private fun notifyClickCallback(venue: MapVenue) {
        if (venue.isSelected) {
            callback.onMapVenueSelected(venue)
        } else {
            callback.onMapVenueDeselected(venue)
        }
    }

    fun onMarkerClicked(clickedMapVenue: MapVenue) {
        val selectedVenue = selectedMapVenue

        try {
            // De-select current selected marker if it is different from clicked one
            if (selectedVenue != null && selectedVenue != clickedMapVenue) {
                val selectedMarker = getMarker(selectedVenue)
                selectedVenue.isSelected = false
                updateMarkerView(clickedMapVenue)
                selectedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            }

            if (selectedVenue == clickedMapVenue) {
                // If selected and current clicked are same then toggle the state
                val clickedMarker = getMarker(clickedMapVenue)
                clickedMapVenue.isSelected = !clickedMapVenue.isSelected
                updateMarkerView(clickedMapVenue)
                notifyClickCallback(clickedMapVenue)
                clickedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            } else {
                // If selected venue is different from clicked then set clicked marker as selected
                val clickedMarker = getMarker(clickedMapVenue)
                clickedMapVenue.isSelected = true
                updateMarkerView(clickedMapVenue)
                notifyClickCallback(clickedMapVenue)
                clickedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            }
        } catch (exception: Exception) {
            Timber.w(exception)
        }

        selectedMapVenue = if (clickedMapVenue.isSelected) {
            clickedMapVenue
        } else {
            null
        }
    }

    fun clearAllSelection() {
        val selectedVenue = selectedMapVenue
        if (selectedVenue != null) {
            clusterManager.algorithm.removeItem(selectedVenue)
            selectedVenue.isSelected = false
            clusterManager.algorithm.addItem(selectedVenue)
        }
        selectedMapVenue = null
    }

    interface Callback {
        fun onMapVenueSelected(venue: MapVenue)
        fun onMapVenueDeselected(venue: MapVenue)
    }
}