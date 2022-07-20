package com.ribbit.data.remote.models.people

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCrossedDto(
        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("conversationId")
        var conversationId: String? = null,

        @field:SerializedName("crossedUserId")
        val crossedUser: ProfileDto? = null,

        @field:SerializedName(value = "senderId", alternate = ["groupId"])
        var profile: ProfileDto? = null,

        @field:SerializedName("time")
        val time: String? = null,

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("isMember")
        var isMember: Boolean? = null,

        @field:SerializedName("location")
        val location: List<Double>? = null) : Parcelable