package com.conversify.ui.chat.individual

import com.conversify.data.remote.models.chat.ChatMessageDto
import com.google.gson.annotations.SerializedName

/**
 * Created by Manish Bhargav on 10/1/19
 */
data class ChatIndividualResponse(
        @field:SerializedName("chatData")
        val chatMessages: List<ChatMessageDto>? = null)