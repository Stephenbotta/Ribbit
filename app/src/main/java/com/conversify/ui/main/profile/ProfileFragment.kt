package com.conversify.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.landing.LandingActivity
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment() {
    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogout.setOnClickListener {
            UserManager.removeProfile()
            startActivity(Intent(requireActivity(), LandingActivity::class.java))
            activity?.finishAffinity()
        }
    }
}