package com.conversify.data.remote.models.venues

import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName

data class VenueDto(
        @field:SerializedName("venueLocationName")
        val venueLocationName: String? = null,

        @field:SerializedName("distance")
        val distance: Double? = null,

        @field:SerializedName("groupId")
        val groupId: String? = null,

        @field:SerializedName("venueTitle")
        val venueName: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("memberCount")
        val memberCount: Int? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("venueLocation")
        val venueLocation: List<Double>? = null,

        var myVenue: Boolean = false)