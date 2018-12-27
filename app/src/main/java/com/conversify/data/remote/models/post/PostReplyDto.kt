package com.conversify.data.remote.models.post

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

@Parcelize
data class PostReplyDto(
        @field:SerializedName("replyCount")
        var replyCount: Int? = null,

        @field:SerializedName("commentBy")
        val commentBy: ProfileDto? = null,

        @field:SerializedName("replyBy")
        val replyBy: ProfileDto? = null,

        @field:SerializedName("comment")
        val comment: String? = null,

        @field:SerializedName("reply")
        val reply: String? = null,

        @field:SerializedName("likeCount")
        var likesCount: Int? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null,

        @field:SerializedName("liked")
        var liked: Boolean? = null,

        var parentReplyId: String? = null,
        var subRepliesLoading: Boolean = false,
        var pendingReplyCount: Int = 0,
        var visibleReplyCount: Int = 0,
        var subReplies: MutableList<PostReplyDto> = mutableListOf()) : Parcelable