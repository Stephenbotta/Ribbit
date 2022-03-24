package com.ribbit.ui.main

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.MessageEvent
import com.ribbit.data.remote.PushType
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.isNetworkActive
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.main.chats.individual.IndividualChatFragment
import com.ribbit.ui.main.explore.ExploreFragment
import com.ribbit.ui.main.home.HomeFragment
import com.ribbit.ui.main.survey.SurveyUserStaticsFragment
import com.ribbit.ui.profile.ProfileFragment
import com.ribbit.utils.AppConstants
import com.ribbit.utils.FragmentSwitcher
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class MainActivity : BaseActivity() {
    companion object {
        private const val TAB_INDEX_HOME = 0
        private const val TAB_INDEX_CHATS = 1
        //        private const val TAB_INDEX_SEARCH_USERS = 2
        private const val TAB_INDEX_EXPLORE = 2
        private const val TAB_INDEX_NOTIFICATIONS = 3
        private const val TAB_INDEX_PROFILE = 4

        private const val EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX"
        private const val EXTRA_SELECTED_FRAGMENT_TAG = "EXTRA_SELECTED_FRAGMENT_TAG"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var fragmentSwitcher: FragmentSwitcher

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createDeviceToken()
        viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flMainContainer)
        if (savedInstanceState == null)
            checkPushNavigation(savedInstanceState)
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

    private fun checkPushNavigation(savedInstanceState: Bundle?) {
        val type = intent.getStringExtra("TYPE")
        if (!type.isNullOrEmpty()) {
            when (type) {
                PushType.CHAT -> {
                    val data = intent.getStringExtra("data")
                    val profile = gson.fromJson(data, ProfileDto::class.java)
                    bottomTabs.getTabAt(TAB_INDEX_CHATS)?.select()
                    val userCrossed = UserCrossedDto()
                    userCrossed.profile = profile
                    userCrossed.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(IndividualChatFragment.TAG))
                        fragmentSwitcher.addFragment(IndividualChatFragment(), IndividualChatFragment.TAG)

                    val intent = ChatActivity.getStartIntentForIndividualChat(this, userCrossed, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
                }
                /*PushType.GROUP_CHAT -> {
                    val data = intent.getStringExtra("data")
                    val profile = gson.fromJson(data, GroupDto::class.java)
                    bottomTabs.getTabAt(TAB_INDEX_CHATS)?.select()
                    profile.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(ChatsFragment.TAG))
                        fragmentSwitcher.addFragment(ChatsFragment(), ChatsFragment.TAG)
                    val intent = ChatActivity.getStartIntentForGroupChat(this, profile, AppConstants.REQ_CODE_GROUP_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_CHAT)
                }*/
                /*PushType.VENUE_CHAT -> {
                    bottomTabs.getTabAt(TAB_INDEX_EXPLORE)?.select()
                    val data = intent.getStringExtra("data")
                    val profile = gson.fromJson(data, VenueDto::class.java)
                    profile.conversationId = intent.getStringExtra("id")
                    if (!fragmentSwitcher.fragmentExist(ExploreFragment.TAG))
                        fragmentSwitcher.addFragment(ExploreFragment(), ExploreFragment.TAG)
                    val intent = ChatActivity.getStartIntent(this, profile, AppConstants.REQ_CODE_VENUE_CHAT)
                    startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                }
                else -> {
                    bottomTabs.getTabAt(TAB_INDEX_NOTIFICATIONS)?.select()
                    if (!fragmentSwitcher.fragmentExist(NotificationsFragment.TAG))
                        fragmentSwitcher.addFragment(NotificationsFragment(), NotificationsFragment.TAG)
                }*/
            }

        } else {
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
                        if (!fragmentSwitcher.fragmentExist(IndividualChatFragment.TAG)) {
                            fragmentSwitcher.addFragment(IndividualChatFragment(), IndividualChatFragment.TAG)
                        }
                    }

                    /* TAB_INDEX_SEARCH_USERS -> {
                         if (!fragmentSwitcher.fragmentExist(SearchUsersFragment.TAG)) {
                             fragmentSwitcher.addFragment(SearchUsersFragment(), SearchUsersFragment.TAG)
                         }
                     }*/

                    TAB_INDEX_EXPLORE -> {
                        if (!fragmentSwitcher.fragmentExist(ExploreFragment.TAG)) {
                            fragmentSwitcher.addFragment(Fragment(), ExploreFragment.TAG)
                        }
                    }

                    TAB_INDEX_NOTIFICATIONS -> {
                        if (!fragmentSwitcher.fragmentExist(SurveyUserStaticsFragment.TAG)) {
                            fragmentSwitcher.addFragment(SurveyUserStaticsFragment(), SurveyUserStaticsFragment.TAG)
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

    /*override fun onLocationUpdated(location: Location) {
        viewModel.currentLocationUpdated(location)
    }*/

    private fun updateNotificationBadgeCount(notificationCount: String) {
        val notificationTab = bottomTabs.getTabAt(TAB_INDEX_NOTIFICATIONS)
        val tabView = notificationTab?.customView
        val badgeText = tabView?.findViewById<TextView>(R.id.tvCount)
        if (notificationCount.isNotEmpty() && notificationCount.toInt() > 0) {
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
        ivNotification?.setImageResource(if (tabCurrentPosition == TAB_INDEX_NOTIFICATIONS) {
            R.drawable.ic_rewards_select
        } else {
            R.drawable.ic_reward_unselect
        })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.type == AppConstants.EVENT_PUSH_NOTIFICATION && isNetworkActive()) {
            viewModel.getNotificationCount()
        }
    }

}