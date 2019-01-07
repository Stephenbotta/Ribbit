package com.conversify.data.remote.models.people

import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

/**
 * Created by Manish Bhargav on 3/1/19.
 */
data class UserCrossedDto(
        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("conversationId")
        val conversationId: String? = null,

        @field:SerializedName("crossedUserId")
        val crossedUser: ProfileDto? = null,

        @field:SerializedName("time")
        val time: ZonedDateTime? = null,

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("location")
        val location: List<Double>? = null

)