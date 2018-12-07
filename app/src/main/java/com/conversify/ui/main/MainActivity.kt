package com.conversify.ui.main

import android.os.Bundle
import android.support.design.widget.TabLayout
import com.conversify.R
import com.conversify.data.remote.socket.SocketManager
import com.conversify.data.repository.InterestsRepository
import com.conversify.ui.base.BaseLocationActivity
import com.conversify.ui.main.chats.ChatsFragment
import com.conversify.ui.main.explore.ExploreFragment
import com.conversify.ui.main.home.HomeFragment
import com.conversify.ui.main.profile.ProfileFragment
import com.conversify.ui.main.searchusers.SearchUsersFragment
import com.conversify.utils.FragmentSwitcher
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : BaseLocationActivity() {
    companion object {
        private const val TAB_INDEX_HOME = 0
        private const val TAB_INDEX_CHATS = 1
        private const val TAB_INDEX_SEARCH_USERS = 2
        private const val TAB_INDEX_EXPLORE = 3
        private const val TAB_INDEX_PROFILE = 4

        private const val EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX"
        private const val EXTRA_SELECTED_FRAGMENT_TAG = "EXTRA_SELECTED_FRAGMENT_TAG"
    }

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flMainContainer)

        if (savedInstanceState == null) {
            // On first launch add home fragment
            fragmentSwitcher.addFragment(HomeFragment(), HomeFragment.TAG)
            Timber.d("Saved instance state is null")
        } else {
            // Restore the tab state to the last selected if activity state is restored
            val tabIndex = savedInstanceState.getInt(EXTRA_SELECTED_TAB_INDEX, TAB_INDEX_HOME)
            if (tabIndex != -1) {
                bottomTabs.getTabAt(tabIndex)?.select()
                Timber.d("Saved instance state exist. Selected tab index : $tabIndex")
            }

            val currentFragmentTag = savedInstanceState.getString(EXTRA_SELECTED_FRAGMENT_TAG)
            fragmentSwitcher.setCurrentFragmentTag(currentFragmentTag)
        }

        setupBottomTabs()

        // Get and cache interests. Callback is not required.
        InterestsRepository.getInstance().getInterests()

        // Connect socket
        SocketManager.getInstance().connect()
    }

    private fun setupBottomTabs() {
        bottomTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_INDEX_HOME -> {
                        if (!fragmentSwitcher.fragmentExist(HomeFragment.TAG)) {
                            fragmentSwitcher.addFragment(HomeFragment(), HomeFragment.TAG)
                        }
                    }

                    TAB_INDEX_CHATS -> {
                        if (!fragmentSwitcher.fragmentExist(ChatsFragment.TAG)) {
                            fragmentSwitcher.addFragment(ChatsFragment(), ChatsFragment.TAG)
                        }
                    }

                    TAB_INDEX_SEARCH_USERS -> {
                        if (!fragmentSwitcher.fragmentExist(SearchUsersFragment.TAG)) {
                            fragmentSwitcher.addFragment(SearchUsersFragment(), SearchUsersFragment.TAG)
                        }
                    }

                    TAB_INDEX_EXPLORE -> {
                        if (!fragmentSwitcher.fragmentExist(ExploreFragment.TAG)) {
                            fragmentSwitcher.addFragment(ExploreFragment(), ExploreFragment.TAG)
                        }
                    }

                    TAB_INDEX_PROFILE -> {
                        if (!fragmentSwitcher.fragmentExist(ProfileFragment.TAG)) {
                            fragmentSwitcher.addFragment(ProfileFragment(), ProfileFragment.TAG)
                        }
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedTabIndex = bottomTabs.selectedTabPosition
        outState.putInt(EXTRA_SELECTED_TAB_INDEX, selectedTabIndex)
        outState.putString(EXTRA_SELECTED_FRAGMENT_TAG, fragmentSwitcher.getCurrentFragmentTag())
        Timber.d("Saving instance state. Selected tab index : $selectedTabIndex")
    }
}