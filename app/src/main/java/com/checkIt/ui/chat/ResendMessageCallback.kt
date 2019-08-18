package com.checkIt.ui.chat

import com.checkIt.data.remote.models.chat.ChatMessageDto

interface ResendMessageCallback {
    fun onResendMessageClicked(chatMessage: ChatMessageDto)
}