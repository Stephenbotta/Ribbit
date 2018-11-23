package com.conversify.data.remote.models.venues

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VenueDto(
        @field:SerializedName("venueLocationName")
        val locationName: String? = null,

        @field:SerializedName("venueLocationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("distance")
        val distance: Double? = null,

        @field:SerializedName("adminId")
        val adminId: String? = null,

        @field:SerializedName("conversationId")
        val conversationId: String? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("venueTitle")
        val name: String? = null,

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
        val dateTimeMillis: Long? = null,

        var myVenue: Boolean = false) : Parcelable