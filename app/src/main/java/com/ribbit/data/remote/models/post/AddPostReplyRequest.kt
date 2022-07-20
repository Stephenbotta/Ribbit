package com.ribbit.data.remote.models.post

import com.google.gson.annotations.SerializedName

data class AddPostReplyRequest(
        @field:SerializedName("postId")
        val postId: String? = null,

        @field:SerializedName("mediaId")
        val mediaId: String? = null,

        @field:SerializedName("postBy")
        val postOwnerId: String? = null,

        @field:SerializedName("comment")
        val replyText: String? = null,

        @field:SerializedName("userIdTag")
        val usernameMentions: List<String>? = null)