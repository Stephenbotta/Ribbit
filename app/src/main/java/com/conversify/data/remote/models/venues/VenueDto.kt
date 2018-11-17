package com.conversify.data.remote.models.venues

import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName

data class VenueDto(
        @field:SerializedName("venueLocationName")
        val venueLocationName: String? = null,

        @field:SerializedName("distance")
        val distance: Double? = null,

        @field:SerializedName("adminId")
        val adminId: String? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("venueTitle")
        val venueName: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("memberCount")
        val memberCount: Int? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("venueTags")
        val tags: List<String>? = null,

        @field:SerializedName("venueLocation")
        val venueLocation: List<Double>? = null,

        @field:SerializedName("venueTime")
        val dateTime: Long? = null,

        var myVenue: Boolean = false)