package com.ribbit.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.ribbit.R
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.main.notifications.NotificationsFragment
import com.ribbit.utils.FragmentSwitcher

class ProfileActivity : BaseActivity() {
    companion object {
        private const val EXTRA_SHOW_PROFILE_SCREEN = "EXTRA_SHOW_PROFILE_SCREEN"

        fun start(context: Context, showProfileScreen: Boolean) {
            context.startActivity(Intent(context, ProfileActivity::class.java)
                    .putExtra(EXTRA_SHOW_PROFILE_SCREEN, showProfileScreen))
        }
    }

    private lateinit var fragmentSwitcher: FragmentSwitcher

    private val showProfileScreen by lazy { intent.getBooleanExtra(EXTRA_SHOW_PROFILE_SCREEN, true) }
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flProfileContainer)

        if (showProfileScreen) {
            fragmentSwitcher.addFragment(ProfileFragment.newInstance(false), ProfileFragment.TAG)
        } else {
            fragmentSwitcher.addFragment(NotificationsFragment(), NotificationsFragment.TAG)
        }
    }
}