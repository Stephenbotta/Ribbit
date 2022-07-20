package com.ribbit.ui.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.loginsignup.SelectedUser
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
        var isBlocked: Boolean? = null,

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

        @field:SerializedName("userType")
        val userType: String? = null,

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
        val interests: ArrayList<InterestDto>? = null,

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

        @field:SerializedName("website")
        var website: String? = null,

        @field:SerializedName("gender")
        var gender: String? = null,

        @field:SerializedName("isUploaded")
        val isUploaded: Boolean? = null,

        @field:SerializedName("isPhoneNumberVerified")
        val isMobileVerified: Boolean? = null,

        @field:SerializedName("isEmailVerified")
        var isEmailVerified: Boolean? = null,

        @field:SerializedName("alertNotifications")
        val isAlertNotifications: Boolean? = null,

        @field:SerializedName("isAccountPrivate")
        val isAccountPrivate: Boolean? = null,

        @field:SerializedName("isPassportVerified")
        var isPassportVerified: Boolean? = null,

        @field:SerializedName("groupFollowed")
        val groupFollowed: MutableList<String>? = null,

        @field:SerializedName("followers")
        val followers: MutableList<String>? = null,

        @field:SerializedName("following")
        val following: MutableList<String>? = null,

        @field:SerializedName("blockedBy")
        val blockedBy: MutableList<String>? = null,

        @field:SerializedName("blockedWhom")
        val blockedWhom: MutableList<String>? = null,

        @field:SerializedName("homeSearchTop")
        val homeSearchTop: MutableList<String>? = null,

        @field:SerializedName("tagsFollowed")
        val tagsFollowed: MutableList<String>? = null,

        @field:SerializedName("imageVisibility")
        var imageVisibility: MutableList<SelectedUser>? = null,

        @field:SerializedName("nameVisibility")
        var nameVisibility: MutableList<SelectedUser>? = null,

        @field:SerializedName("tagPermission")
        var tagPermission: MutableList<SelectedUser>? = null,

        @field:SerializedName("personalInfoVisibility")
        var personalInfoVisibility: MutableList<SelectedUser>? = null,

        @field:SerializedName("locationVisibility")
        val locationVisibility: Boolean? = null,

        @field:SerializedName("imageVisibilityForFollowers")
        val imageVisibilityForFollowers: Boolean? = null,

        @field:SerializedName("nameVisibilityForFollowers")
        val nameVisibilityForFollowers: Boolean? = null,

        @field:SerializedName("tagPermissionForFollowers")
        val tagPermissionForFollowers: Boolean? = null,

        @field:SerializedName("personalInfoVisibilityForFollowers")
        val personalInfoVisibilityForFollowers: Boolean? = null,

        @field:SerializedName("imageVisibilityForEveryone")
        val imageVisibilityForEveryone: Boolean? = null,

        @field:SerializedName("nameVisibilityForEveryone")
        val nameVisibilityForEveryone: Boolean? = null,

        @field:SerializedName("tagPermissionForEveryone")
        val tagPermissionForEveryone: Boolean? = null,

        @field:SerializedName("requestPending")
        var isRequestPending: Boolean? = null,

        @field:SerializedName("distance")
        val distance: Double? = null,

        @field:SerializedName("isTakeSurvey")
        var isTakeSurvey: Boolean? = null,

        @field:SerializedName("totalSurveys")
        var totalSurveys: Int? = null,

        @field:SerializedName("pointEarned")
        var pointEarned: Int? = null,

        @field:SerializedName("pointRedeemed")
        var pointRedeemed: Int? = null,

        var isSelected: Boolean = false) : Parcelable