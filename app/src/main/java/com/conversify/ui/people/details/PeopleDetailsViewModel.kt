package com.conversify.ui.people.details

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseViewModel

class PeopleDetailsViewModel(application: Application) : BaseViewModel(application) {

    val profileDto = MutableLiveData<ProfileDto>()


}