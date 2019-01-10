package com.conversify.ui.chat

import com.conversify.data.remote.models.chat.ChatMessageDto

interface ResendMessageCallback {
    fun onResendMessageClicked(chatMessage: ChatMessageDto)
}