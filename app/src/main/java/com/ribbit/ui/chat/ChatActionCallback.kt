package com.ribbit.ui.chat

import com.ribbit.data.remote.models.chat.ChatMessageDto

interface ChatActionCallback {
    fun onDeleteImage(chatMessage: ChatMessageDto)
    fun onImageShow(chatMessage: ChatMessageDto)
    fun onVideoShow(chatMessage: ChatMessageDto)
}