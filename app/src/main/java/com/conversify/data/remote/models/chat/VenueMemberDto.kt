package com.conversify.data.remote.models.chat

import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName

data class VenueMemberDto(
        @field:SerializedName("isAdmin")
        val isAdmin: Boolean? = null,

        @field:SerializedName("userId")
        val user: ProfileDto? = null)