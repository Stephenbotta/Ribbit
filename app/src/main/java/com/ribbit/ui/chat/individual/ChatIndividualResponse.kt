package com.ribbit.ui.chat.individual

import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.chat.ChatMessageDto

/**
 * Created by Manish Bhargav
 */
data class ChatIndividualResponse(
        @field:SerializedName("chatData")
        val chatMessages: List<ChatMessageDto>? = null)