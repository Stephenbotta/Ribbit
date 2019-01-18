package com.conversify.ui.main.searchusers

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.ui.base.BaseViewModel

/**
 * Created by Manish Bhargav on 18/1/19
 */
class SearchUsersViewModel(application: Application):BaseViewModel(application) {

    private var profile = UserManager.getProfile()



    fun getProfile() = profile

    fun profileUpdated() {
        profile = UserManager.getProfile()
    }

}