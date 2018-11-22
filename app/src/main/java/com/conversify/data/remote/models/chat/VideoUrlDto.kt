package com.conversify.data.remote.models.chat

import com.google.gson.annotations.SerializedName

data class VideoUrlDto(
        @field:SerializedName("thumbnail")
        val thumbnail: String? = null,

        @field:SerializedName("original")
        val original: String? = null)