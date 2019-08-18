package com.checkIt.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("otp")
        val otp: String? = null,

        @field:SerializedName("countryCode")
        val countryCode: String? = null,

        @field:SerializedName("phoneNumber")
        val phoneNumber: String? = null)