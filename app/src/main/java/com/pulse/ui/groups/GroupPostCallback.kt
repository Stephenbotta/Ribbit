package com.pulse.ui.groups

import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.data.remote.models.loginsignup.InterestDto

interface GroupPostCallback : PostCallback {
    fun onGroupClicked(group: GroupDto)
    fun onGroupCategoryClicked(category: InterestDto)
}