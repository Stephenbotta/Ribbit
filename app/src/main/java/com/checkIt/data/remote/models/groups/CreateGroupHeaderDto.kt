package com.checkIt.data.remote.models.groups

import java.io.File

data class CreateGroupHeaderDto(val categoryName: String?,
                                val categoryId: String?,
                                var groupTitle: String? = null,
                                var selectedGroupImageFile: File? = null,
                                var isPrivate: Boolean = false,
                                var description: String? = "",
                                var memberCount: Int = 0)