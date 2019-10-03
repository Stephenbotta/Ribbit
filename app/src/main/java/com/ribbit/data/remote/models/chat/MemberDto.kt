package com.ribbit.data.remote.models.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MemberDto(
        @field:SerializedName("isAdmin")
        val isAdmin: Boolean? = null,

        @field:SerializedName("userId")
        val user: ProfileDto? = null) : Parcelable