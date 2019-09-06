package com.checkIt.ui.chat

import com.checkIt.data.remote.models.chat.ChatMessageDto

interface ChatActionCallback {
    fun onDeleteImage(chatMessage: ChatMessageDto)
    fun onImageShow(chatMessage: ChatMessageDto)
    fun onVideoShow(chatMessage: ChatMessageDto)
}