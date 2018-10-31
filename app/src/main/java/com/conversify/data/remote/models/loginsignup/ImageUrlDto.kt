package com.conversify.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageUrlDto(
        @field:SerializedName("thumbnail")
        val thumbnail: String? = null,

        @field:SerializedName("original")
        val original: String? = null) : Parcelable