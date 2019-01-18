package com.conversify.ui.search.groups

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment


class SearchGroupFragment : BaseFragment() {

    companion object {
        const val TAG = "SearchGroupFragment"
    }


    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun observeChanges() {

    }

    fun search(query: String?) {

    }
}