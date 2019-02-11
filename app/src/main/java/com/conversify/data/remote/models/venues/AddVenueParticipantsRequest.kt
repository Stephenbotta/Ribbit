package com.conversify.data.remote.models.venues

import com.google.gson.annotations.SerializedName

data class AddVenueParticipantsRequest(
        @field:SerializedName("venueId")
        val venueId: String,

        @field:SerializedName("groupId")
        val groupId: String,

        @field:SerializedName("participants")
        val participantIds: List<String>)