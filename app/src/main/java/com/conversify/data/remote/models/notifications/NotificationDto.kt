package com.conversify.data.remote.models.notifications

import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.data.remote.models.venues.VenueDto
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

data class NotificationDto(
        @field:SerializedName("venueId")
        val venue: VenueDto? = null,

        @field:SerializedName("groupId")
        val group: GroupDto? = null,

        @field:SerializedName("isRead")
        val isRead: Boolean? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("byId")
        val sender: ProfileDto? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("postId")
        val postId: GroupPostDto? = null,

        @field:SerializedName("commentId")
        val commentId: PostReplyDto? = null,

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("location")
        val location: ArrayList<Double>? = null,

        @field:SerializedName("toId")
        val toId: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null)