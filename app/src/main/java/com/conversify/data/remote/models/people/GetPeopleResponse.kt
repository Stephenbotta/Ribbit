package com.conversify.data.remote.models.people

import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

/**
 * Created by Manish Bhargav
 */
data class GetPeopleResponse(

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("timestamp")
        val timestamp: ZonedDateTime? = null,

        @field:SerializedName("userCrossed")
        val userCrossed: List<UserCrossedDto>? = null

)