package com.checkIt.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InterestDto(
        @field:SerializedName("imageUrl")
        val image: ImageUrlDto? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("categoryName")
        val name: String? = null,

        var selected: Boolean = false):Parcelable