package com.conversify.data.remote.models.groups

import android.os.Parcelable
import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupDto(
        @field:SerializedName("groupName")
        val name: String? = null,

        @field:SerializedName("memberCounts")
        var memberCount: Int? = null,

        @field:SerializedName("unReadCounts")
        val unreadCount: Int? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("adminId")
        val adminId: String? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("isMember")
        var isMember: Boolean? = null) : Parcelable