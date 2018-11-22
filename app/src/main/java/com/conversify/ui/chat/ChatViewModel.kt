package com.conversify.ui.chat

import android.arch.lifecycle.ViewModel
import com.conversify.data.local.UserManager
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.utils.SingleLiveEvent

class ChatViewModel : ViewModel() {
    private val ownUserId by lazy { UserManager.getUserId() }

    val newMessage by lazy { SingleLiveEvent<ChatMessageDto>() }

    private val chatMessageBuilder by lazy { ChatMessageBuilder(ownUserId) }

    fun sendTextMessage(message: String) {
        newMessage.value = chatMessageBuilder.buildTextMessage(message)
    }
}