package com.conversify.ui.loginsignup.loginpassword

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.clickSpannable
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_login_password.*

class LoginPasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginPasswordFragment"
        private const val ARGUMENT_REGISTERED_MODE = "ARGUMENT_REGISTERED_MODE"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"

        fun newInstance(registeredMode: Int, profile: ProfileDto): Fragment {
            val fragment = LoginPasswordFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_REGISTERED_MODE, registeredMode)
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registeredMode = arguments?.getInt(ARGUMENT_REGISTERED_MODE)
                ?: AppConstants.REGISTERED_MODE_PHONE

        val profile = arguments?.getParcelable<ProfileDto>(ARGUMENT_PROFILE)
        if (profile != null) {
            tvName.text = profile.fullName
            if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
                tvPhoneOrEmail.text = String.format("%s %s", profile.countryCode, profile.phoneNumber)
            } else {
                tvPhoneOrEmail.text = profile.email
            }

            GlideApp.with(this)
                    .load(profile.image?.thumbnail)
                    .placeholder(R.color.greyImageBackground)
                    .error(R.color.greyImageBackground)
                    .into(ivProfile)
        }

        setListeners()
    }

    private fun setListeners() {
        fabProceed.setOnClickListener {
            etPassword.clearFocus()
        }

        val spannableText = getString(R.string.login_label_reset_password)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.brandon_text_bold)
        tvLabelForgotPassword.clickSpannable(spannableText, R.color.colorPrimary, typeface,
                View.OnClickListener {})
    }
}