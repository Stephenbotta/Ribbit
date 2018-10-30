package com.conversify.ui.loginsignup.createpassword

import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.welcome.WelcomeFragment
import kotlinx.android.synthetic.main.fragment_create_password.*

class CreatePasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "CreatePasswordFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabProceed.setOnClickListener {
            val fragment = WelcomeFragment()
            fragmentManager?.apply {
                beginTransaction()
                        .add(R.id.flContainer, fragment, WelcomeFragment.TAG)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }
}