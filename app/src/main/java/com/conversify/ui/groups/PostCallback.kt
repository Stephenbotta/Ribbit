package com.conversify.ui.groups

import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto

interface PostCallback {
    fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean)
    fun onLikesCountClicked(post: GroupPostDto)
    fun onUserProfileClicked(profile: ProfileDto)
    fun onHashtagClicked(tag: String)
    fun onUsernameMentionClicked(username: String)
}