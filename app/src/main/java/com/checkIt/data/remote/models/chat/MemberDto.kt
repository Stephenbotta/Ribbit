package com.checkIt.data.remote.models.chat

import android.os.Parcelable
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemberDto(
        @field:SerializedName("isAdmin")
        val isAdmin: Boolean? = null,

        @field:SerializedName("userId")
        val user: ProfileDto? = null) : Parcelable