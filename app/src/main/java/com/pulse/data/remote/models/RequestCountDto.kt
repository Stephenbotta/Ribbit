package com.pulse.data.remote.models

import com.google.gson.annotations.SerializedName

data class RequestCountDto(
        @field:SerializedName("requestCount")
        val requestCount: String? = null
)