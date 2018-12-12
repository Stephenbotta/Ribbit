package com.conversify.data.remote.models.notifications

import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueDto
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

data class NotificationDto(
        @field:SerializedName("venueId")
        val venue: VenueDto? = null,

        @field:SerializedName("isRead")
        val isRead: Boolean? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("byId")
        val sender: ProfileDto? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null)