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

        @field:SerializedName(value = "fullName", alternate = ["adminId"])
        var fullName: String? = null,

        @field:SerializedName("followingCount")
        var followingCount: Long? = null,

        @field:SerializedName("followerCount")
        var followersCount: Long? = null,

        @field:SerializedName(value = "userName", alternate = ["groupName"])
        var userName: String? = null,

        @field:SerializedName("bio")
        val bio: String? = null,

        @field:SerializedName("age")
        val age: Int? = null,

        @field:SerializedName("company")
        val company: String? = null,

        @field:SerializedName("designation")
        val designation: String? = null,

        @field:SerializedName("accessToken")
        val accessToken: String? = null,

        @field:SerializedName("isPasswordExist")
        val isPasswordExist: Boolean? = null,

        @field:SerializedName("phoneNumber")
        val phoneNumber: String? = null,

        @field:SerializedName("fullphoneNumber")
        val fullPhoneNumber: String? = null,

        @field:SerializedName("isDeleted")
        val isDeleted: Boolean? = null,

        @field:SerializedName("countryCode")
        val countryCode: String? = null,

        @field:SerializedName("imageUrl")
        var image: ImageUrlDto? = null,

        @field:SerializedName("_id")
        var id: String? = null,

        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("googleId")
        val googleId: String? = null,

        @field:SerializedName("facebookId")
        val facebookId: String? = null,

        @field:SerializedName("interestTags")
        val interests: List<InterestDto>? = null,

        @field:SerializedName("isProfileComplete")
        val isProfileComplete: Boolean? = null,

        @field:SerializedName("groupCount")
        var groupCount: Int? = null,

        @field:SerializedName("IsOnline")
        val IsOnline: Boolean? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("isFollowing")
        var isFollowing: Boolean? = null,

        @field:SerializedName("conversationId")
        var conversationId: String? = null,

        @field:SerializedName("tagName")
        var tagName: String? = null,

        var isSelected: Boolean = false) : Parcelable