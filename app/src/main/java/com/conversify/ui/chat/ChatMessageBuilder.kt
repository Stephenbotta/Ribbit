package com.conversify.ui.chat

import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.models.chat.ChatMessageDetailsDto
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.chat.MessageStatus
import com.conversify.data.remote.models.loginsignup.ProfileDto
import org.json.JSONObject
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

class ChatMessageBuilder(private val ownUserId: String) {
    fun buildTextMessage(message: String): ChatMessageDto {
        val details = ChatMessageDetailsDto(message = message,
                type = ApiConstants.MESSAGE_TYPE_TEXT)

        return ChatMessageDto(isDelivered = false,
                createdDateTime = ZonedDateTime.now(),
                messageStatus = MessageStatus.SENDING,
                ownMessage = true,
                sender = ProfileDto(id = ownUserId),
                details = details)
    }

    fun getChatMessageFromSocketArgument(argument: Any?): ChatMessageDto? {
        return if (argument is JSONObject) {
            try {
                RetrofitClient.GSON.fromJson(argument.toString(), ChatMessageDto::class.java)
            } catch (exception: Exception) {
                Timber.w(exception)
                null
            }
        } else {
            null
        }
    }
}