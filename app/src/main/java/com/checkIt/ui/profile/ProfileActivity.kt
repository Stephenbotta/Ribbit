package com.checkIt.ui.profile

import android.os.Bundle
import com.checkIt.R
import com.checkIt.ui.base.BaseActivity
import com.checkIt.utils.FragmentSwitcher

class ProfileActivity : BaseActivity() {

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flProfileContainer)
        fragmentSwitcher.addFragment(ProfileFragment(), ProfileFragment.TAG)
    }
}