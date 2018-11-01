package com.conversify.data.remote.models.loginsignup

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
        @field:SerializedName("flag")
        val flag: Int,

        @field:SerializedName("fullName")
        val fullName: String? = null,

        @field:SerializedName("userName")
        val userName: String? = null,

        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("countryCode")
        val countryCode: String? = null,

        @field:SerializedName("phoneNumber")
        val phoneNumber: String? = null,

        @field:SerializedName("facebookId")
        val facebookId: String? = null,

        @field:SerializedName("googleId")
        val googleId: String? = null,

        @field:SerializedName("password")
        val password: String? = null)