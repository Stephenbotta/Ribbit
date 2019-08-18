package com.checkIt.ui.groups

import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.data.remote.models.loginsignup.ProfileDto

interface PostCallback {
    fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean)
    fun onLikesCountClicked(post: GroupPostDto)
    fun onGroupPostLikeClicked(groupPost: GroupPostDto, isLiked: Boolean)
    fun onUserProfileClicked(profile: ProfileDto)
    fun onHashtagClicked(tag: String)
    fun onUsernameMentionClicked(username: String)
    fun onAddCommentClicked(post: GroupPostDto, comment: String)
}