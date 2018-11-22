package com.conversify.ui.chat

import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.chat.ChatMessageDetailsDto
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.chat.MessageStatus
import com.conversify.data.remote.models.loginsignup.ProfileDto

class ChatMessageBuilder(private val ownUserId: String) {
    fun buildTextMessage(message: String): ChatMessageDto {
        val details = ChatMessageDetailsDto(message = message,
                type = ApiConstants.MESSAGE_TYPE_TEXT)

        return ChatMessageDto(isDelivered = false,
                createdDateTimeMillis = System.currentTimeMillis(),
                messageStatus = MessageStatus.SENDING,
                ownMessage = true,
                sender = ProfileDto(id = ownUserId),
                details = details)
    }
}