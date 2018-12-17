package com.conversify.data.remote.models.groups

data class CreateGroupHeaderDto(val categoryName: String,
                                val categoryId: String,
                                var groupTitle: String? = null,
                                var groupImage: String? = null,
                                var isPrivate: Boolean = false)