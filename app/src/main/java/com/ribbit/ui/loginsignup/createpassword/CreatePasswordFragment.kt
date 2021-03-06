package com.ribbit.ui.loginsignup.createpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.shortToast
import com.ribbit.extensions.updateAlphaLevel
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.loginsignup.welcome.WelcomeFragment
import com.ribbit.utils.ValidationUtils
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

        fabProceed.isEnabled = false
        fabProceed.updateAlphaLevel()
        setListeners()
    }

    private fun setListeners() {
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val password = text?.toString() ?: ""
                fabProceed.isEnabled = ValidationUtils.isPasswordLengthValid(password)
                fabProceed.updateAlphaLevel()
            }
        })

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
                                    .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                            R.anim.parallax_left_in, R.anim.parallax_right_out)
                                    .replace(R.id.flContainer, fragment, WelcomeFragment.TAG)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }
                }
            }
        }
    }
}