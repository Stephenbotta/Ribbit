package com.conversify.data.remote.models.groups

import com.google.gson.annotations.SerializedName

data class CreateEditGroupRequest(
        @field:SerializedName("postGroupId")
        val id: String? = null,

        @field:SerializedName("groupName")
        var title: String? = null,

        @field:SerializedName("categoryId")
        val categoryId: String? = null,

        @field:SerializedName("isPrivate")
        var isPrivate: Int? = null,

        @field:SerializedName("groupImageOriginal")
        var imageOriginalUrl: String? = null,

        @field:SerializedName("groupImageThumbnail")
        var imageThumbnailUrl: String? = null,

        @field:SerializedName("participantIds")
        var participantIds: List<String>? = null)