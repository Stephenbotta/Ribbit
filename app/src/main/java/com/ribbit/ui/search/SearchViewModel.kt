package com.ribbit.ui.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ribbit.ui.base.BaseViewModel

class SearchViewModel(application: Application) : BaseViewModel(application) {
    val search = MutableLiveData<String>()
}