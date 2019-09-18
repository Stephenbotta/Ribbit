package com.checkIt.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageUrlDto(
        @field:SerializedName("_id")
        var id: String? = null,

        @field:SerializedName("thumbnail")
        var thumbnail: String? = null,

        @field:SerializedName("original")
        var original: String? = null,

        @field:SerializedName("mediaType")
        var mediaType: String? = null,

        @field:SerializedName("videoUrl")
        var videoUrl: String? = null,

        @field:SerializedName("liked")
        var isLiked: Boolean? = null,

        @field:SerializedName("isMostLiked")
        var isMostLiked: Boolean? = null,

        @field:SerializedName("likeCount")
        var likesCount: Int? = null
) : Parcelable