package com.conversify.data.remote.models.people

import com.google.gson.annotations.SerializedName

/**
 * Created by Manish Bhargav on 3/1/19.
 */
data class UserCrossedDto(

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("conversationId")
        val conversationId: String? = null,

        @field:SerializedName("crossedUserId")
        val crossedUserId: UserDetails? = null,

        @field:SerializedName("time")
        val time: String? = null,

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("location")
        val location: List<Double>? = null

)