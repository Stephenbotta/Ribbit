package com.pulse.ui.groups

import com.pulse.data.remote.models.groups.GroupPostDto
import com.pulse.data.remote.models.loginsignup.ProfileDto

interface PostCallback {
    fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean)
    fun onLikesCountClicked(post: GroupPostDto)
    fun onGroupPostLikeClicked(groupPost: GroupPostDto, isLiked: Boolean)
    fun onUserProfileClicked(profile: ProfileDto)
    fun onHashtagClicked(tag: String)
    fun onUsernameMentionClicked(username: String)
    fun onAddCommentClicked(post: GroupPostDto, comment: String)
}