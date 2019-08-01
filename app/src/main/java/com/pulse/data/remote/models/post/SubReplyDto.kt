package com.pulse.data.remote.models.post

data class SubReplyDto(val parentReply: PostReplyDto,
                       val replies: List<PostReplyDto>)