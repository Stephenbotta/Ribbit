package com.conversify.ui.createvenue

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_venue_categories.*

class VenueCategoriesFragment : BaseFragment() {
    companion object {
        const val TAG = "VenueCategoriesFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venue_categories

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvVenueCategories.adapter = VenueCategoriesAdapter()
    }
}