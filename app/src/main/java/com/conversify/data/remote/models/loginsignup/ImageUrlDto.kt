package com.conversify.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageUrlDto(
        @field:SerializedName("thumbnail")
        var thumbnail: String? = null,

        @field:SerializedName("original")
        var original: String? = null) : Parcelable