package com.conversify.ui.loginsignup.createpassword

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.welcome.WelcomeFragment
import com.conversify.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_create_password.*

class CreatePasswordFragment : BaseFragment() {
    companion object {
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        const val TAG = "CreatePasswordFragment"

        fun newInstance(profile: ProfileDto): Fragment {
            val fragment = CreatePasswordFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabProceed.setOnClickListener {
            val password = etPassword.text.toString()

            when {
                password.isEmpty() -> {
                    requireActivity().shortToast(R.string.error_empty_password)
                }

                !ValidationUtils.isPasswordLengthValid(password) -> {
                    requireActivity().shortToast(R.string.error_invalid_password_length)
                }

                else -> {
                    val profile = arguments?.getParcelable<ProfileDto>(ARGUMENT_PROFILE)
                    if (profile != null) {
                        val fragment = WelcomeFragment.newInstance(profile, password)
                        fragmentManager?.apply {
                            beginTransaction()
                                    .add(R.id.flContainer, fragment, WelcomeFragment.TAG)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }
                }
            }
        }
    }
}