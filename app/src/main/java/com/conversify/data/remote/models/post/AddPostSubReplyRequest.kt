package com.conversify.data.remote.models.post

import com.google.gson.annotations.SerializedName

data class AddPostSubReplyRequest(
        @field:SerializedName("commentId")
        val topLevelReplyId: String? = null,

        @field:SerializedName("commentBy")
        val topLevelReplyOwnerId: String? = null,

        @field:SerializedName("postId")
        val postId: String? = null,

        @field:SerializedName("reply")
        val replyText: String? = null,

        @field:SerializedName("userIdTag")
        val usernameMentions: List<String>? = null)