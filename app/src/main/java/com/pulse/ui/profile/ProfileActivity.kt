package com.pulse.ui.profile

import android.os.Bundle
import com.pulse.R
import com.pulse.ui.base.BaseActivity
import com.pulse.utils.FragmentSwitcher

class ProfileActivity : BaseActivity() {

    private lateinit var fragmentSwitcher: FragmentSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fragmentSwitcher = FragmentSwitcher(supportFragmentManager, R.id.flProfileContainer)
        fragmentSwitcher.addFragment(ProfileFragment(), ProfileFragment.TAG)
    }
}