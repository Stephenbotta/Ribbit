package com.conversify.ui.search.venues

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment


class SearchVenueFragment : BaseFragment() {

    companion object {
        const val TAG = "SearchVenueFragment"
    }


    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_venue

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