package com.ribbit.ui.people

import com.ribbit.data.remote.models.people.UserCrossedDto

interface PeopleCallback {
    fun onClickItem(user: UserCrossedDto, isDetailShow: Boolean)
}