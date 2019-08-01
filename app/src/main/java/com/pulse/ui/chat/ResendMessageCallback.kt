package com.pulse.ui.chat

import com.pulse.data.remote.models.chat.ChatMessageDto

interface ResendMessageCallback {
    fun onResendMessageClicked(chatMessage: ChatMessageDto)
}