package com.conversify.ui.groups

import com.conversify.data.remote.models.groups.GroupDto

interface GroupPostCallback : PostCallback {
    fun onGroupClicked(group: GroupDto)
}