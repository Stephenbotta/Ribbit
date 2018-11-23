package com.conversify.data.remote.models.chat

import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime
import java.io.File

data class ChatMessageDto(
        @field:SerializedName("isDelivered")
        val isDelivered: Boolean? = null,

        @field:SerializedName("senderId")
        val sender: ProfileDto? = null,

        @field:SerializedName("conversationId")
        val conversationId: String? = null,

        @field:SerializedName("createdDate")
        val createdDateTime: ZonedDateTime? = null,

        @field:SerializedName("chatDetails")
        val details: ChatMessageDetailsDto? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        var localId: String? = null,
        var localFile: File? = null,
        var localFileThumbnail: File? = null,
        var messageStatus: MessageStatus = MessageStatus.SENT,
        var ownMessage: Boolean = false,
        var showDate: Boolean = false,
        var showProfileImage: Boolean = false)