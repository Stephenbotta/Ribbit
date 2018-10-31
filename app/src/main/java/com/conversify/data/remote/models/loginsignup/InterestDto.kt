package com.conversify.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class InterestDto(
        @field:SerializedName("imageUrl")
        val image: ImageUrlDto? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("categoryName")
        val name: String? = null,

        var selected: Boolean = false)