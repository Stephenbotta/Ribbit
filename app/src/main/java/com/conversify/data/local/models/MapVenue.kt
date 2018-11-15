package com.conversify.data.local.models

import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.toLatLng
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MapVenue(val venue: VenueDto,
                    var isSelected: Boolean = false) : ClusterItem {
    private val latLng = venue.venueLocation.toLatLng()

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return venue.memberCount.toString()
    }

    override fun getPosition(): LatLng {
        return latLng
    }
}