package com.conversify.data.remote.models.groups

import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName

data class GroupDto(
        @field:SerializedName("groupName")
        val name: String? = null,

        @field:SerializedName("memberCounts")
        val memberCount: Int? = null,

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
        val isMember: Boolean? = null)