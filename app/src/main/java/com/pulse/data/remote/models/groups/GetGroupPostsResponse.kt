package com.pulse.data.remote.models.groups

import com.google.gson.annotations.SerializedName

data class GetGroupPostsResponse(
        @field:SerializedName("conversData")
        val posts: List<GroupPostDto>? = null)