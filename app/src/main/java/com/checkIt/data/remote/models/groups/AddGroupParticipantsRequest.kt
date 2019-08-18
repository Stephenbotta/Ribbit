package com.checkIt.data.remote.models.groups

import com.google.gson.annotations.SerializedName

data class AddGroupParticipantsRequest(
        @field:SerializedName("groupId")
        val groupId: String,

        @field:SerializedName("participants")
        val participantIds: List<String>)