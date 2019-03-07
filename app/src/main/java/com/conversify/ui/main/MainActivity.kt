package com.conversify.ui.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.widget.ImageView
import android.widget.TextView
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.remote.PushType
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.gone
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseLocationActivity
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.main.chats.ChatsFragment
import com.conversify.ui.main.explore.ExploreFragment
import com.conversify.ui.main.home.HomeFragment
import com.conversify.ui.main.notifications.NotificationsFragment
import com.conversify.ui.main.searchusers.SearchUsersFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.FragmentSwitcher
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : BaseLocationActivity() {
    companion object {
        private const val TAB_INDEX_HOME = 0
        private const val TAB_INDEX_CHATS = 1
        private const val TAB_INDEX_SEARCH_USERS = 2
        private const val TAB_INDEX_EXPLORE = 3
        //        private const val TAB_INDEX_PROFILE = 4
        private const val TAB_INDEX_NOTIFICATIONS = 4

        private const val EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX"
        private const val EXTRA_SELECTED_FRAGMENT_TAG = "EXTRA_SELECTED_FRAGMENT_TAG"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createDeviceToken()
        viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flMainContainer)
        if (savedInstanceState == null) {
            // On first launch add home fragment
//            fragmentSwitcher.addFragment(HomeFragment(), HomeFragment.TAG)
            Timber.d("Saved instance state is null")
            checkPushNavigation()
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
        if (isNetworkActive())
            viewModel.getNotificationCount()
        observeNotificationCount()
    }

    private fun observeNotificationCount() {
        viewModel.notificationCount.observe(this, Observer {
            updateNotificationBadgeCount(it ?: "")
        })
    }

    private fun checkPushNavigation() {
        val type = intent.getStringExtra("TYPE")
        if (!type.isNullOrEmpty()) {
            when (type) {
                PushType.CHAT -> {
                    val data = intent.getParcelableExtra<ProfileDto>("data")
                    bottomTabs.getTabAt(TAB_INDEX_CHATS)?.select()
                    val userCrossed = UserCrossedDto()
                    userCrossed.profile = data
                    userCrossed.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(ChatsFragment.TAG))
                        fragmentSwitcher.addFragment(ChatsFragment(), ChatsFragment.TAG)

                    val intent = ChatActivity.getStartIntentForIndividualChat(this, userCrossed, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
                }
                PushType.GROUP_CHAT -> {
                    val data = intent.getParcelableExtra<GroupDto>("data")
                    bottomTabs.getTabAt(TAB_INDEX_CHATS)?.select()
                    data.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(ChatsFragment.TAG))
                        fragmentSwitcher.addFragment(ChatsFragment(), ChatsFragment.TAG)
                    val intent = ChatActivity.getStartIntentForGroupChat(this, data, AppConstants.REQ_CODE_GROUP_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_CHAT)
                }
                PushType.VENUE_CHAT -> {
                    bottomTabs.getTabAt(TAB_INDEX_EXPLORE)?.select()
                    val data = intent.getParcelableExtra<VenueDto>("data")
                    data.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(ExploreFragment.TAG))
                        fragmentSwitcher.addFragment(ExploreFragment(), ExploreFragment.TAG)
                    val intent = ChatActivity.getStartIntent(this, data, AppConstants.REQ_CODE_VENUE_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                }
                else -> {
                    bottomTabs.getTabAt(TAB_INDEX_NOTIFICATIONS)?.select()

                    if (!fragmentSwitcher.fragmentExist(NotificationsFragment.TAG))
                        fragmentSwitcher.addFragment(NotificationsFragment(), NotificationsFragment.TAG)
                }
            }

        } else {
            fragmentSwitcher.addFragment(HomeFragment(), HomeFragment.TAG)

        }

    }


    private fun createDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.d("getInstanceId failed : ${task.exception}")
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val token = task.result?.token
                    UserManager.saveDeviceToken(token.toString())
                })
    }

    private fun setupBottomTabs() {
        bottomTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                updateNotificationIcon(tab.position)
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

                    TAB_INDEX_NOTIFICATIONS -> {
                        if (!fragmentSwitcher.fragmentExist(NotificationsFragment.TAG)) {
                            fragmentSwitcher.addFragment(NotificationsFragment(), NotificationsFragment.TAG)
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

    override fun onLocationUpdated(location: Location) {
        viewModel.currentLocationUpdated(location)
    }

    private fun updateNotificationBadgeCount(notificationCount: String) {
        val notificationTab = bottomTabs.getTabAt(TAB_INDEX_NOTIFICATIONS)
        val tabView = notificationTab?.customView
        val badgeText = tabView?.findViewById<TextView>(R.id.tvCount)
        if (notificationCount.toInt() > 0) {
            badgeText?.visible()
            badgeText?.text = notificationCount
        } else {
            badgeText?.gone()
        }
    }

    private fun updateNotificationIcon(tabCurrentPosition: Int) {
        val notificationTab = bottomTabs.getTabAt(TAB_INDEX_NOTIFICATIONS)
        val tabView = notificationTab?.customView
        val ivNotification = tabView?.findViewById<ImageView>(R.id.ivNotification)
        if (tabCurrentPosition == TAB_INDEX_NOTIFICATIONS) {
            ivNotification?.setImageResource(R.drawable.ic_notification)
        } else {
            ivNotification?.setImageResource(R.drawable.ic_notification_gray)
        }
    }
}