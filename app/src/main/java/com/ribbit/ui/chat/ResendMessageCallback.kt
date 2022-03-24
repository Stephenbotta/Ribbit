package com.ribbit.ui.chat

import com.ribbit.data.remote.models.chat.ChatMessageDto

interface ResendMessageCallback {
    fun onResendMessageClicked(chatMessage: ChatMessageDto)
}