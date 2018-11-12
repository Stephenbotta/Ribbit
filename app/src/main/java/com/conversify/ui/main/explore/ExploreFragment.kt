package com.conversify.ui.main.explore

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.groups.GroupsFragment
import com.conversify.ui.people.PeopleFragment
import com.conversify.ui.venues.VenuesListFragment
import com.conversify.utils.FragmentSwitcher
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment : BaseFragment() {
    companion object {
        const val TAG = "ExploreFragment"

        private const val TAB_INDEX_GROUPS = 0
        private const val TAB_INDEX_VENUES = 1
        private const val TAB_INDEX_PEOPLE = 2
    }

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_explore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentSwitcher = FragmentSwitcher(childFragmentManager, R.id.flExploreContainer)

        btnNotification.setOnClickListener { }

        setupTabs()
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_INDEX_GROUPS -> {
                        if (!fragmentSwitcher.fragmentExist(GroupsFragment.TAG)) {
                            fragmentSwitcher.addFragment(GroupsFragment(), GroupsFragment.TAG)
                        }
                    }

                    TAB_INDEX_VENUES -> {
                        if (!fragmentSwitcher.fragmentExist(VenuesListFragment.TAG)) {
                            fragmentSwitcher.addFragment(VenuesListFragment(), VenuesListFragment.TAG)
                        }
                    }

                    TAB_INDEX_PEOPLE -> {
                        if (!fragmentSwitcher.fragmentExist(PeopleFragment.TAG)) {
                            fragmentSwitcher.addFragment(PeopleFragment(), PeopleFragment.TAG)
                        }
                    }
                }
            }
        })
    }
}