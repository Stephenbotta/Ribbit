package com.checkIt.ui.groups

import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.models.loginsignup.InterestDto

interface GroupPostCallback : PostCallback {
    fun onGroupClicked(group: GroupDto)
    fun onGroupCategoryClicked(category: InterestDto)
}