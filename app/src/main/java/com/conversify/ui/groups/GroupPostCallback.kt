package com.conversify.ui.groups

import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.InterestDto

interface GroupPostCallback : PostCallback {
    fun onGroupClicked(group: GroupDto)
    fun onGroupCategoryClicked(category: InterestDto)
}