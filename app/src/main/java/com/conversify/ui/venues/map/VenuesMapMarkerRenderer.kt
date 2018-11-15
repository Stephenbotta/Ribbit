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

class VenuesMapMarkerRenderer(context: Context,
                              googleMap: GoogleMap,
                              clusterManager: ClusterManager<MapVenue>) : DefaultClusterRenderer<MapVenue>(context.applicationContext, googleMap, clusterManager) {
    private var mapMarkerView = View.inflate(context, R.layout.layout_map_venue, null)
    private val iconGenerator = IconGenerator(context)
    private val textColorNormal = ContextCompat.getColor(context, R.color.colorPrimary)
    private val textColorSelected = ContextCompat.getColor(context, R.color.white)
    //private val selectedBackgroundColor = ContextCompat.getColor(context, R.color.green)

    init {
        iconGenerator.setContentView(mapMarkerView)
    }

    override fun onBeforeClusterItemRendered(item: MapVenue, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)

        mapMarkerView.tvMemberCount.text = item.title
        /*if (item.isSelected) {
            mapMarkerView.tvPrice.setTextColor(Color.WHITE)
            iconGenerator.setColor(selectedBackgroundColor)
        } else {*/
        mapMarkerView.tvMemberCount.setTextColor(textColorNormal)
        iconGenerator.setBackground(null)
        //}

        markerOptions.flat(true)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
    }

    /*fun updateMarkers(selectedMapProperty: MapProperty?, clickedMapProperty: MapProperty) {
        if (selectedMapProperty != null) {
            val selectedMarker = getMarker(selectedMapProperty)
            mapMarkerView.tvPrice.setTextColor(Color.BLACK)
            iconGenerator.setColor(Color.WHITE)
            selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
        }

        val clickedMarker = getMarker(clickedMapProperty)
        mapMarkerView.tvPrice.setTextColor(Color.WHITE)
        iconGenerator.setColor(selectedBackgroundColor)
        clickedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
    }*/
}