package com.ribbit.ui.main.explore

import android.os.Bundle
import android.view.View
import com.ribbit.R
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.groups.listing.GroupsFragment
import com.ribbit.utils.FragmentSwitcher

class ExploreFragment : BaseFragment()/*, VenuesModeNavigator */ {
    companion object {
        const val TAG = "ExploreFragment"

        private const val TAB_INDEX_GROUPS = 0
        /*private const val TAB_INDEX_VENUES = 1
        private const val TAB_INDEX_PEOPLE = 2*/

        /*private const val VENUES_MODE_LIST = 0
        private const val VENUE_MODE_MAP = 1*/
    }

    private lateinit var fragmentSwitcher: FragmentSwitcher
//    private var displayedVenuesMode = VENUES_MODE_LIST

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_explore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentSwitcher = FragmentSwitcher(childFragmentManager, R.id.flExploreContainer)

        if (!fragmentSwitcher.fragmentExist(GroupsFragment.TAG)) {
            fragmentSwitcher.addFragment(GroupsFragment(), GroupsFragment.TAG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
    }

    private fun setupTabs() {
        /*tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
                        navigateToVenues()
                    }

                    TAB_INDEX_PEOPLE -> {
                        if (!fragmentSwitcher.fragmentExist(PeopleFragment.TAG)) {
                            fragmentSwitcher.addFragment(PeopleFragment(), PeopleFragment.TAG)
                        }
                    }
                }
            }
        })*/
    }

    /*private fun navigateToVenues() {
        when (displayedVenuesMode) {
            VENUES_MODE_LIST -> {
                if (!fragmentSwitcher.fragmentExist(VenuesListFragment.TAG)) {
                    fragmentSwitcher.addFragment(VenuesListFragment(), VenuesListFragment.TAG)
                }
            }

            VENUE_MODE_MAP -> {
                if (!fragmentSwitcher.fragmentExist(VenuesMapFragment.TAG)) {
                    fragmentSwitcher.addFragment(VenuesMapFragment(), VenuesMapFragment.TAG)
                }
            }
        }
    }*/

    /*override fun navigateToVenuesList() {
        displayedVenuesMode = VENUES_MODE_LIST
        navigateToVenues()
    }

    override fun navigateToVenuesMap() {
        displayedVenuesMode = VENUE_MODE_MAP
        navigateToVenues()
    }*/
}