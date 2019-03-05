package com.conversify.data.remote.models.groups

import android.os.Parcelable
import com.conversify.data.remote.models.chat.MemberDto
import com.conversify.data.remote.models.loginsignup.ImageUrlDto
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupDto(
        @field:SerializedName("groupName")
        val name: String? = null,

        @field:SerializedName(value = "memberCounts", alternate = ["memberCount"])
        var memberCount: Int? = null,

        @field:SerializedName("unReadCounts")
        var unreadCount: Int? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("adminId")
        val adminId: String? = null,

        @field:SerializedName("_id")//, alternate = ["groupId"]
        var id: String? = null,

        @field:SerializedName("requestStatus")
        var requestStatus: String? = null,

        @field:SerializedName("participationRole")
        var participationRole: String? = null,

        @field:SerializedName("createdBy")
        val createdBy: String? = null,

        @field:SerializedName("conversationId")
        var conversationId: String? = null,

        @field:SerializedName("isPrivate")
        val isPrivate: Boolean? = null,

        @field:SerializedName("isMember")
        var isMember: Boolean? = null,

        @field:SerializedName("notification")
        var notification: Boolean? = null,

        @field:SerializedName("description")
        val description: String? = null,

        @field:SerializedName("membersList")
        var members: List<MemberDto>? = null) : Parcelable