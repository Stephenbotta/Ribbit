package com.conversify.ui.main.profile

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.extensions.startLandingWithClear
import com.conversify.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment() {
    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogout.setOnClickListener {
            requireActivity().startLandingWithClear()
        }
    }
}