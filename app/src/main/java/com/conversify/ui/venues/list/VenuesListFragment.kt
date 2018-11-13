package com.conversify.ui.venues.list

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_venues_list.*

class VenuesListFragment : BaseFragment() {
    companion object {
        const val TAG = "VenuesListFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabAddVenue.setOnClickListener { }

        btnMapVenues.setOnClickListener {}
        btnVenuesFilter.setOnClickListener { }
    }
}