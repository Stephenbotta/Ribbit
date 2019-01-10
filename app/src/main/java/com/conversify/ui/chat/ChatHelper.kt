package com.conversify.ui.chat

import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.extensions.isSameDay

object ChatHelper {
    fun updateCurrentMessage(currentPosition: Int, currentMessage: ChatMessageDto, aboveMessage: ChatMessageDto?) {
        var showDate = false
        if (currentPosition == 0) {
            showDate = true
        } else {
            val currentDateTime = currentMessage.createdDateTime
            val aboveDateTime = aboveMessage?.createdDateTime
            if (currentDateTime != null && aboveDateTime != null) {
                if (!currentDateTime.isSameDay(aboveDateTime)) {
                    showDate = true
                }
            }
        }

        var showProfileImage = false
        if (!currentMessage.ownMessage) {
            if (currentPosition == 0 || currentMessage.sender?.id != aboveMessage?.sender?.id) {
                showProfileImage = true
            }
        }

        currentMessage.showDate = showDate
        currentMessage.showProfileImage = showProfileImage
    }
}