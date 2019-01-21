package com.conversify.ui.profile

import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.FragmentSwitcher

class ProfileActivity : BaseActivity() {

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flProfileContainer)
        fragmentSwitcher.addFragment(ProfileFragment(), ProfileFragment.TAG)

    }

}