package com.checkIt.data.remote.models.chat

import com.google.gson.annotations.SerializedName

data class VideoUrlDto(
        @field:SerializedName("thumbnail")
        var thumbnail: String? = null,

        @field:SerializedName("original")
        var original: String? = null)