package com.pulse.data.remote.models.venues

import android.os.Parcelable
import com.pulse.data.remote.models.chat.MemberDto
import com.pulse.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

@Parcelize
data class VenueDto(
        @field:SerializedName("venueLocationName")
        val locationName: String? = null,

        @field:SerializedName("venueLocationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("distance")
        var distance: Double? = null,

        @field:SerializedName("adminId")
        val adminId: String? = null,

        @field:SerializedName("conversationId")
        var conversationId: String? = null,

        @field:SerializedName(value = "_id", alternate = ["venueId"])
        var id: String? = null,

        @field:SerializedName("venueTitle")
        val name: String? = null,

        @field:SerializedName("requestStatus")
        var requestStatus: String? = null,

        @field:SerializedName("participationRole")
        var participationRole: String? = null,

        @field:SerializedName("createdBy")
        val createdBy: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("memberCount")
        var memberCount: Int? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("venueTags")
        val tags: List<String>? = null,

        @field:SerializedName("venueLocation")
        val venueLocation: List<Double>? = null,

        @field:SerializedName("venueTime")
        val venueDateTime: ZonedDateTime? = null,

        @field:SerializedName("notification")
        var notification: Boolean? = null,

        @field:SerializedName("isMember")
        var isMember: Boolean? = null,

        @field:SerializedName("membersList")
        var members: ArrayList<MemberDto>? = null) : Parcelable