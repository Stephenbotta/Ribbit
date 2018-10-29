package com.conversify.ui.loginsignup.loginpassword

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_login_password.*

class LoginPasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginPasswordFragment"
        private const val ARGUMENT_REGISTERED_MODE = "ARGUMENT_REGISTERED_MODE"

        fun newInstance(registeredMode: Int): Fragment {
            val fragment = LoginPasswordFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_REGISTERED_MODE, registeredMode)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registeredMode = arguments?.getInt(ARGUMENT_REGISTERED_MODE)
                ?: AppConstants.REGISTERED_MODE_PHONE

        // todo update profile
        tvName.text = ""
        if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
            tvPhoneNumber.text = ""
        } else {
            tvPhoneNumber.text = ""
        }

        GlideApp.with(this)
                .load(R.color.greyImageBackground)
                .placeholder(R.color.greyImageBackground)
                .error(R.color.greyImageBackground)
                .into(ivProfile)
    }
}