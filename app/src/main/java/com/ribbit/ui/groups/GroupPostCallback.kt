package com.ribbit.ui.groups

import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.loginsignup.InterestDto

interface GroupPostCallback : PostCallback {
    fun onGroupClicked(group: GroupDto)
    fun onGroupCategoryClicked(category: InterestDto)
}