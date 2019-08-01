package com.pulse.ui.loginsignup.loginpassword

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.pulse.R
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.loginsignup.LoginRequest
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.*
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.custom.LoadingDialog
import com.pulse.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.pulse.ui.main.MainActivity
import com.pulse.utils.GlideApp
import com.pulse.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_login_password.*

class LoginPasswordFragment : BaseFragment() {
    companion object {
        const val TAG = "LoginPasswordFragment"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        private const val ARGUMENT_CREDENTIALS = "ARGUMENT_CREDENTIALS"

        fun newInstance(profile: ProfileDto, credentials: String): Fragment {
            val fragment = LoginPasswordFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            arguments.putString(ARGUMENT_CREDENTIALS, credentials)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: LoginPasswordViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var profile: ProfileDto
    private lateinit var credentials: String

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[LoginPasswordViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        credentials = arguments?.getString(ARGUMENT_CREDENTIALS) ?: ""

        fabProceed.isEnabled = false    // Disabled by default

        displayProfile()
        setListeners()
        observeChanges()
    }

    private fun displayProfile() {
        tvName.text = profile.fullName
        tvPhoneOrEmail.text = credentials

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
                    val request = LoginRequest(credentials = credentials, password = password)
                    viewModel.login(request)
                }
            }
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val password = text?.toString() ?: ""
                fabProceed.isEnabled = ValidationUtils.isPasswordLengthValid(password)
            }
        })

        val spannableText = getString(R.string.login_label_reset_password)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.roboto_text_bold)
        tvLabelForgotPassword.clickSpannable(spannableText, R.color.colorPrimary, typeface,
                View.OnClickListener {
                    if (isNetworkActiveWithMessage()) {
                        viewModel.resetPassword(profile.email ?: "")
                    }
                })
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
                        val fragment = ChooseInterestsFragment.newInstance(interest = resource.data?.interests
                                ?: arrayListOf())
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

        viewModel.resetPassword.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    requireActivity().longToast(R.string.login_message_reset_password_link_sent)
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