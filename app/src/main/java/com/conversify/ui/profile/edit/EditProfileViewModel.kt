package com.conversify.ui.profile.edit

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.ui.base.BaseViewModel

/**
 * Created by Manish Bhargav
 */
class EditProfileViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()


    fun getProfile() = profile

}