package com.conversify.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class LoginRequest(
        // Containing either of email, username, phone number or phone number with country code.
        @field:SerializedName("userCredentials")
        val credentials: String? = null,

        @field:SerializedName("facebookId")
        val facebookId: String? = null,

        @field:SerializedName("googleId")
        val googleId: String? = null,

        @field:SerializedName("password")
        val password: String? = null)