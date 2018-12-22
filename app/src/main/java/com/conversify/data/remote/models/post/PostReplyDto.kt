package com.conversify.data.remote.models.post

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

@Parcelize
data class PostReplyDto(
        @field:SerializedName("replyCount")
        val replyCount: Int? = null,

        @field:SerializedName("commentBy")
        val commentBy: ProfileDto? = null,

        @field:SerializedName("replyBy")
        val replyBy: ProfileDto? = null,

        @field:SerializedName("comment")
        val comment: String? = null,

        @field:SerializedName("reply")
        val reply: String? = null,

        @field:SerializedName("likeCount")
        val likeCount: Int? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null,

        @field:SerializedName("liked")
        val liked: Boolean? = null) : Parcelable