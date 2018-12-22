package com.conversify.data.remote.models.post

import com.conversify.data.remote.models.groups.GroupPostDto

data class SubReplyDto(val parentPost: GroupPostDto,
                       val replies: List<GroupPostDto>)