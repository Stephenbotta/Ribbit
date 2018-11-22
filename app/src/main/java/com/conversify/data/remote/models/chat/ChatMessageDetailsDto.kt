package com.conversify.data.remote.models.chat

import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName

data class ChatMessageDetailsDto(
        @field:SerializedName("videoUrl")
        val video: VideoUrlDto? = null,

        @field:SerializedName("imageUrl")
        val image: ImageUrlDto? = null,

        @field:SerializedName("userIdTags")
        val userIdTags: List<Any>? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("message")
        val message: String? = null)