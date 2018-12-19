package com.conversify.data.remote.models.groups

import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

data class GroupPostDto(
        @field:SerializedName("postText")
        val postText: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("postBy")
        val user: ProfileDto? = null,

        @field:SerializedName("likeCount")
        val likesCount: Int? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null,

        @field:SerializedName("liked")
        val isLiked: Boolean? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("groupId")
        val group: GroupDto? = null,

        @field:SerializedName("hashTags")
        val hashTags: List<String>? = null,

        @field:SerializedName("commentCount")
        val commentsCount: Int? = null)