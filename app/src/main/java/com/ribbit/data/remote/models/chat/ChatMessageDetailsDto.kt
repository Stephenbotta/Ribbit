package com.ribbit.data.remote.models.chat

import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto

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