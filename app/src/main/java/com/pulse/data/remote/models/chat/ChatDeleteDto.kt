package com.pulse.data.remote.models.chat

import com.google.gson.annotations.SerializedName

data class ChatDeleteDto(

        @field:SerializedName("senderId")
        val senderId: String? = null,

        @field:SerializedName(value = "receiverId", alternate = ["groupId"])
        val receiverId: String? = null,

        @field:SerializedName("messageId")
        val messageId: String? = null,

        @field:SerializedName("type")
        val type: String? = null
)