package com.checkIt.ui.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.checkIt.ui.base.BaseViewModel

class SearchViewModel(application: Application) : BaseViewModel(application) {
    val search = MutableLiveData<String>()
}