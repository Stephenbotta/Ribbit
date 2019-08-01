package com.pulse.ui.chat.individual

import com.pulse.data.remote.models.chat.ChatMessageDto
import com.google.gson.annotations.SerializedName

/**
 * Created by Manish Bhargav
 */
data class ChatIndividualResponse(
        @field:SerializedName("chatData")
        val chatMessages: List<ChatMessageDto>? = null)