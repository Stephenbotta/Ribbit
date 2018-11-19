package com.conversify.ui.loginsignup.welcome

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.loginsignup.SignUpRequest
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.BackButtonEnabledListener
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.conversify.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment() {
    companion object {
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        private const val ARGUMENT_PASSWORD = "ARGUMENT_PASSWORD"
        const val TAG = "WelcomeFragment"

        fun newInstance(profile: ProfileDto, password: String? = null): Fragment {
            val fragment = WelcomeFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            if (password != null) {
                arguments.putString(ARGUMENT_PASSWORD, password)
            }
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: WelcomeViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var profile: ProfileDto
    private var backButtonEnabledListener: BackButtonEnabledListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BackButtonEnabledListener) {
            backButtonEnabledListener = context
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_welcome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        backButtonEnabledListener?.onBackButtonEnabled(false)

        viewModel = ViewModelProviders.of(this)[WelcomeViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())

        setupViews()
        setListeners()
        observeChanges()
    }

    private fun setupViews() {
        etFullName.setText(profile.fullName)

        // Hide email fields if email exists in profile
        if (!profile.email.isNullOrBlank()) {
            tvLabelEmail.gone()
            etEmail.gone()
        } else {
            etEmail.setText(profile.email)
        }

        // Hide phone number fields if phone number exists in profile
        if (!profile.phoneNumber.isNullOrBlank()) {
            tvLabelPhoneNumber.gone()
            countryCodePicker.gone()
            etPhoneNumber.gone()
        }
    }

    private fun setListeners() {
        fabProceed.setOnClickListener {
            // Visible in all cases
            val fullName = etFullName.text.toString()
            val username = etUsername.text.toString()

            // Visible if email is not available
            val email = etEmail.text.toString()

            // Visible if phone number is not available
            val phoneNumber = etPhoneNumber.text.toString()

            when {
                fullName.isBlank() -> {
                    requireActivity().shortToast(R.string.error_empty_full_name)
                }

                username.isBlank() -> {
                    requireActivity().shortToast(R.string.error_empty_user_name)
                }

                !ValidationUtils.isUsernameLengthValid(username) -> {
                    requireActivity().shortToast(R.string.error_invalid_user_name_length)
                }

                username.contains(" ") -> {
                    requireActivity().shortToast(R.string.error_user_name_contains_spaces)
                }

                !ValidationUtils.isUsernameCharactersValid(username) -> {
                    requireActivity().longToast(R.string.error_invalid_username_characters)
                }

                etEmail.isVisible() && email.isEmpty() -> {
                    requireActivity().shortToast(R.string.error_empty_email)
                }

                etEmail.isVisible() && !ValidationUtils.isEmailValid(email) -> {
                    requireActivity().shortToast(R.string.error_invalid_email)
                }

                etPhoneNumber.isVisible() && phoneNumber.isEmpty() -> {
                    requireActivity().shortToast(R.string.error_empty_phone_number)
                }

                etPhoneNumber.isVisible() && !ValidationUtils.isPhoneNumberLengthValid(phoneNumber) -> {
                    requireActivity().shortToast(R.string.error_invalid_phone_number)
                }

                isNetworkActiveWithMessage() -> {
                    val flag = when {
                        !profile.googleId.isNullOrBlank() -> ApiConstants.FLAG_REGISTER_GOOGLE
                        !profile.facebookId.isNullOrBlank() -> ApiConstants.FLAG_REGISTER_FACEBOOK
                        !profile.email.isNullOrBlank() -> ApiConstants.FLAG_REGISTER_EMAIL
                        else -> ApiConstants.FLAG_REGISTER_PHONE_NUMBER
                    }

                    val requestEmail = if (etEmail.isVisible()) {
                        email
                    } else {
                        profile.email
                    }
                    val requestCountryCode = if (etPhoneNumber.isVisible()) {
                        countryCodePicker.selectedCountryCodeWithPlus
                    } else {
                        profile.countryCode
                    }
                    val requestPhoneNumber = if (etPhoneNumber.isVisible()) {
                        phoneNumber
                    } else {
                        profile.phoneNumber
                    }

                    val request = SignUpRequest(
                            flag = flag,
                            fullName = fullName,
                            userName = username,
                            email = requestEmail,
                            countryCode = requestCountryCode,
                            phoneNumber = requestPhoneNumber,
                            password = arguments?.getString(ARGUMENT_PASSWORD),
                            googleId = if (flag == ApiConstants.FLAG_REGISTER_GOOGLE) {
                                profile.googleId
                            } else {
                                null
                            },
                            facebookId = if (flag == ApiConstants.FLAG_REGISTER_FACEBOOK) {
                                profile.facebookId
                            } else {
                                null
                            })

                    viewModel.signUp(request)
                }
            }
        }
    }

    private fun observeChanges() {
        viewModel.signUp.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val fragment = ChooseInterestsFragment()
                    fragmentManager?.apply {
                        beginTransaction()
                                .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                        R.anim.parallax_left_in, R.anim.parallax_right_out)
                                .replace(R.id.flContainer, fragment, ChooseInterestsFragment.TAG)
                                .commit()
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