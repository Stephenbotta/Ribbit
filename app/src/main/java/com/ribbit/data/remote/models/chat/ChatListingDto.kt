package com.ribbit.data.remote.models.chat

import com.google.gson.annotations.SerializedName
import com.ribbit.ui.loginsignup.ProfileDto

/**
 * Created by Manish Bhargav
 */
data class ChatListingDto(

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("conversationId")
        var conversationId: String? = null,

        @field:SerializedName("createdDate")
        val createdDate: String? = null,

        @field:SerializedName("unreadCount")
        val unreadCount: Int? = null,

        @field:SerializedName("lastChatDetails")
        val lastChatDetails: ChatMessageDetailsDto? = null,

        @field:SerializedName(value = "senderId", alternate = ["groupId"])
        val profile: ProfileDto? = null


)