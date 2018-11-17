package com.conversify.ui.loginsignup.loginpassword

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.LoginRequest
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.clickSpannable
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.conversify.ui.main.MainActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.conversify.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_login_password.*

class LoginPasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginPasswordFragment"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        private const val ARGUMENT_REGISTERED_MODE = "ARGUMENT_REGISTERED_MODE"

        fun newInstance(profile: ProfileDto, registeredMode: Int): Fragment {
            val fragment = LoginPasswordFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            arguments.putInt(ARGUMENT_REGISTERED_MODE, registeredMode)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: LoginPasswordViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var profile: ProfileDto
    private var registeredMode: Int = AppConstants.REGISTERED_MODE_PHONE

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[LoginPasswordViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        registeredMode = arguments?.getInt(ARGUMENT_REGISTERED_MODE) ?: AppConstants.REGISTERED_MODE_PHONE

        displayProfile()
        setListeners()
        observeChanges()
    }

    private fun displayProfile() {
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

    private fun setListeners() {
        fabProceed.setOnClickListener {
            etPassword.clearFocus()
            val password = etPassword.text.toString()
            when {
                password.isEmpty() -> {
                    requireActivity().shortToast(R.string.error_empty_password)
                }

                !ValidationUtils.isPasswordLengthValid(password) -> {
                    requireActivity().shortToast(R.string.error_invalid_password_length)
                }

                requireActivity().isNetworkActiveWithMessage() -> {
                    val request = if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
                        // Parameters for phone login
                        LoginRequest(countryCode = profile.countryCode,
                                phoneNumber = profile.phoneNumber,
                                password = password)
                    } else {
                        // Parameters for email login
                        LoginRequest(email = profile.email,
                                password = password)
                    }
                    viewModel.login(request)
                }
            }
        }

        val spannableText = getString(R.string.login_label_reset_password)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.brandon_text_bold)
        tvLabelForgotPassword.clickSpannable(spannableText, R.color.colorPrimary, typeface,
                View.OnClickListener {})
    }

    private fun observeChanges() {
        viewModel.login.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    if (resource.data?.isInterestSelected == true) {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finishAffinity()
                    } else {
                        val fragment = ChooseInterestsFragment()
                        fragmentManager?.apply {
                            beginTransaction()
                                    .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                            R.anim.parallax_left_in, R.anim.parallax_right_out)
                                    .add(R.id.flContainer, fragment, ChooseInterestsFragment.TAG)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}