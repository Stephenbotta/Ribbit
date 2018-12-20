package com.conversify.data.remote.models.post

data class CreatePostRequest(val groupId: String? = null,
                             val postText: String? = null,
                             var imageOriginal: String? = null,
                             var imageThumbnail: String? = null,
                             val hashTags: List<String>? = null)