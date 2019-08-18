package com.checkIt.ui.search

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.checkIt.ui.base.BaseViewModel

/**
 * Created by Manish Bhargav
 */
class SearchViewModel(application: Application) : BaseViewModel(application) {

    val search = MutableLiveData<String>()


}