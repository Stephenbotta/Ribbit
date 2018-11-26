package com.conversify.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class UsernameAvailabilityResponse(
        @field:SerializedName("customMessage")
        val customMessage: String? = null,

        @field:SerializedName("type")
        val isAvailable: Boolean? = null)