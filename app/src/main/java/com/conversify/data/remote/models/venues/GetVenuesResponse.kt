package com.conversify.data.remote.models.venues

import com.google.gson.annotations.SerializedName

data class GetVenuesResponse(
        @field:SerializedName("yourVenueData")
        val myVenues: List<VenueDto>? = null,

        @field:SerializedName("venueNearYou")
        val nearbyVenues: List<VenueDto>? = null)