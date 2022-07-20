package com.ribbit.data.local.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.toLatLng

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