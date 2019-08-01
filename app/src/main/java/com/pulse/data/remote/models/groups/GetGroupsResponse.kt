package com.pulse.data.remote.models.groups

import com.google.gson.annotations.SerializedName

data class GetGroupsResponse(
        @field:SerializedName("yourGroups")
        val yourGroups: List<GroupDto>? = null,

        @field:SerializedName("suggestedGroups")
        val suggestedGroups: List<GroupDto>? = null)