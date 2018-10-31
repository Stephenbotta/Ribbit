package com.conversify.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProfileDto(
        @field:SerializedName("isInterestSelected")
        val isInterestSelected: Boolean? = null,

        @field:SerializedName("isVerified")
        val isVerified: Boolean? = null,

        @field:SerializedName("isPasswordReset")
        val isPasswordReset: Boolean? = null,

        @field:SerializedName("isBlocked")
        val isBlocked: Boolean? = null,

        @field:SerializedName("fullName")
        val fullName: String? = null,

        @field:SerializedName("userName")
        val userName: String? = null,

        @field:SerializedName("accessToken")
        val accessToken: String? = null,

        @field:SerializedName("isPasswordExist")
        val isPasswordExist: Boolean? = null,

        @field:SerializedName("phoneNumber")
        val phoneNumber: String? = null,

        @field:SerializedName("isDeleted")
        val isDeleted: Boolean? = null,

        @field:SerializedName("countryCode")
        val countryCode: String? = null,

        @field:SerializedName("imageUrl")
        val image: ImageUrlDto? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("isProfileComplete")
        val isProfileComplete: Boolean? = null) : Parcelable