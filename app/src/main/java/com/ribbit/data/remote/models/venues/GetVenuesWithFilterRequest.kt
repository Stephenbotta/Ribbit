package com.ribbit.data.remote.models.venues

import com.google.gson.annotations.SerializedName

data class GetVenuesWithFilterRequest(
        @field:SerializedName("categoryId")
        val categoryIds: List<String>? = null,

        @field:SerializedName("date")
        val date: String? = null,

        @field:SerializedName("private")
        val privacy: List<Int>? = null,

        @field:SerializedName("locationLat")
        val latitude: Double? = null,

        @field:SerializedName("locationLong")
        val longitude: Double? = null)