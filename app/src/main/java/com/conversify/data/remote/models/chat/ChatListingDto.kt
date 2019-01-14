package com.conversify.data.remote.models.chat

import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName

/**
 * Created by Manish Bhargav on 14/1/19
 */
data class ChatListingDto(

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("conversationId")
        val conversationId: String? = null,

        @field:SerializedName("createdDate")
        val createdDate: String? = null,

        @field:SerializedName("unreadCount")
        val unreadCount: Int? = null,

        @field:SerializedName("lastChatDetails")
        val lastChatDetails: ChatMessageDetailsDto? = null,

        @field:SerializedName(value = "senderId", alternate = ["groupId"])
        val profile: ProfileDto?=null


)