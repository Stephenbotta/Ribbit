package com.ribbit.ui.chat

import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.models.chat.*
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import org.json.JSONObject
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.io.File
import java.util.*

class ChatMessageBuilder(private val ownUserId: String) {
    fun buildTextMessage(message: String): ChatMessageDto {
        val details = ChatMessageDetailsDto(message = message,
                type = ApiConstants.MESSAGE_TYPE_TEXT)
        return getCommonChatMessage(details)
    }

    fun buildImageMessage(image: File): ChatMessageDto {
        val details = ChatMessageDetailsDto(type = ApiConstants.MESSAGE_TYPE_IMAGE,
                image = ImageUrlDto())
        val message = getCommonChatMessage(details)
        message.localFile = image
        message.localFileThumbnail = image
        return message
    }

    fun buildGifMessage(image: File): ChatMessageDto {
        val details = ChatMessageDetailsDto(type = ApiConstants.MESSAGE_TYPE_GIF,
                image = ImageUrlDto())
        val message = getCommonChatMessage(details)
        message.localFile = image
        message.localFileThumbnail = image
        return message
    }

    fun buildVideoMessage(video: File, thumbnailImage: File): ChatMessageDto {
        val details = ChatMessageDetailsDto(type = ApiConstants.MESSAGE_TYPE_VIDEO,
                image = ImageUrlDto(),
                video = VideoUrlDto())
        val message = getCommonChatMessage(details)
        message.localFile = video
        message.localFileThumbnail = thumbnailImage
        return message
    }

    private fun getCommonChatMessage(details: ChatMessageDetailsDto): ChatMessageDto {
        return ChatMessageDto(
                localId = UUID.randomUUID().toString(),
                isDelivered = false,
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

    fun getChatDeleteMessageFromSocketArgument(argument: Any?): ChatDeleteDto? {
        return if (argument is JSONObject) {
            try {
                RetrofitClient.GSON.fromJson(argument.toString(), ChatDeleteDto::class.java)
            } catch (exception: Exception) {
                Timber.w(exception)
                null
            }
        } else {
            null
        }
    }
}