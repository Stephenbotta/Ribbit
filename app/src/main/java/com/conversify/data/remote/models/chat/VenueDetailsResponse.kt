package com.conversify.data.remote.models.chat

import com.google.gson.annotations.SerializedName

data class VenueDetailsResponse(
        @field:SerializedName("chatData")
        val chatMessages: List<ChatMessageDto>? = null,

        @field:SerializedName("groupData")
        val venueMembers: List<VenueMemberDto>? = null)