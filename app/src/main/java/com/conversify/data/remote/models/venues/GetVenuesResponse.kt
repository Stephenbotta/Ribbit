package com.conversify.data.remote.models.venues

import com.google.gson.annotations.SerializedName

data class GetVenuesResponse(
        @field:SerializedName("yourVenueData")
        val yourVenues: List<VenueDto>? = null,

        @field:SerializedName("venueNearYou")
        val venuesNearYou: List<VenueDto>? = null)