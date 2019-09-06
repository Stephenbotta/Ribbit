package com.checkIt.ui.people

import com.checkIt.data.remote.models.people.UserCrossedDto

interface PeopleCallback {
    fun onClickItem(user: UserCrossedDto, isDetailShow: Boolean)
}