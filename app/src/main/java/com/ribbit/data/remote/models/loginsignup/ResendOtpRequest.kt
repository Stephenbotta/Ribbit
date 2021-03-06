package com.ribbit.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class ResendOtpRequest(
        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("countryCode")
        val countryCode: String? = null,

        @field:SerializedName("phoneNumber")
        val phoneNumber: String? = null)