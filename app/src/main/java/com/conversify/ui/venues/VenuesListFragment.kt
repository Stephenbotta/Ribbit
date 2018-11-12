package com.conversify.ui.venues

import com.conversify.R
import com.conversify.ui.base.BaseFragment

class VenuesListFragment : BaseFragment() {
    companion object {
        const val TAG = "VenuesListFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_list
}