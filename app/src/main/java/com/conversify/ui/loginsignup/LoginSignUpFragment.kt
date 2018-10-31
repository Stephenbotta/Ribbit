package com.conversify.ui.loginsignup

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.loginpassword.LoginPasswordFragment
import com.conversify.ui.loginsignup.verification.VerificationFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_login_sign_up.*

class LoginSignUpFragment : BaseFragment(), TextWatcher {
    companion object {
        const val TAG = "LoginSignUpFragment"
        private const val ARGUMENT_MODE = "ARGUMENT_MODE"

        fun newInstance(mode: Int): Fragment {
            val fragment = LoginSignUpFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_MODE, mode)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: LoginSignUpViewModel
    private lateinit var loadingDialog: LoadingDialog
    private var mode = AppConstants.MODE_LOGIN
    private var registeredMode = AppConstants.REGISTERED_MODE_PHONE

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_sign_up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[LoginSignUpViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        mode = arguments?.getInt(ARGUMENT_MODE) ?: AppConstants.MODE_LOGIN

        if (mode == AppConstants.MODE_LOGIN) {
            tvLabelTitle.setText(R.string.login)
        } else {
            tvLabelTitle.setText(R.string.sign_up)
        }

        fabProceed.isEnabled = false
        setListeners()
        observeChanges()
    }

    private fun setListeners() {
        val selectedTextColor = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)
        val deSelectedTextColor = ContextCompat.getColor(requireActivity(), R.color.textGray)

        btnPhoneNumber.setOnClickListener {
            fabProceed.isEnabled = false    // Disable fab
            registeredMode = AppConstants.REGISTERED_MODE_PHONE     // Update registered mode
            btnPhoneNumber.setTextColor(selectedTextColor)
            btnEmail.setTextColor(deSelectedTextColor)
            countryCodePicker.visible()
            etPhoneNumber.visible()
            etPhoneNumber.requestFocus()
            etEmail.setText("")
            etEmail.gone()
        }

        btnEmail.setOnClickListener {
            fabProceed.isEnabled = false    // Disable fab
            registeredMode = AppConstants.REGISTERED_MODE_EMAIL     // Update registered mode
            btnEmail.setTextColor(selectedTextColor)
            btnPhoneNumber.setTextColor(deSelectedTextColor)
            countryCodePicker.gone()
            etEmail.visible()
            etEmail.requestFocus()
            etPhoneNumber.setText("")
            etPhoneNumber.gone()
        }

        etPhoneNumber.addTextChangedListener(this)
        etEmail.addTextChangedListener(this)

        cvContinueWithFacebook.setOnClickListener { }
        cvContinueWithGoogle.setOnClickListener { }

        fabProceed.setOnClickListener {
            if (formDataValid() && requireActivity().isNetworkActiveWithMessage()) {
                if (mode == AppConstants.MODE_LOGIN) {

                } else {
                    if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
                        val countryCode = countryCodePicker.selectedCountryCodeWithPlus
                        val phoneNumber = etPhoneNumber.text.toString()
                        viewModel.registerEmailOrPhoneNumber(countryCode = countryCode, phoneNumber = phoneNumber)
                    } else {
                        val email = etEmail.text.toString()
                        viewModel.registerEmailOrPhoneNumber(email = email)
                    }
                }
            }
        }
    }

    private fun observeChanges() {
        viewModel.registerEmailOrPhone.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val profile = resource.data
                    if (profile != null) {
                        val fragment: Fragment
                        val fragmentTag: String
                        if (mode == AppConstants.MODE_LOGIN) {
                            fragment = LoginPasswordFragment.newInstance(registeredMode, profile)
                            fragmentTag = LoginPasswordFragment.TAG
                        } else {
                            fragment = VerificationFragment.newInstance(registeredMode, profile)
                            fragmentTag = VerificationFragment.TAG
                        }
                        fragmentManager?.apply {
                            beginTransaction()
                                    .add(R.id.flContainer, fragment, fragmentTag)
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

    private fun formDataValid(): Boolean {
        return if (mode == AppConstants.MODE_LOGIN) {
            // Validations for phone number
            val phoneNumber = etPhoneNumber.text.toString()

            if (phoneNumber.isEmpty()) {
                requireActivity().shortToast(R.string.error_empty_phone_number)
                false
            } else if (!ValidationUtils.isPhoneNumberLengthValid(phoneNumber)) {
                requireActivity().shortToast(R.string.error_invalid_phone_number)
                false
            } else {
                true
            }
        } else {
            // Validations for email
            val email = etEmail.text.toString()

            if (email.isEmpty()) {
                requireActivity().shortToast(R.string.error_empty_email)
                false
            } else if (!ValidationUtils.isEmailValid(email)) {
                requireActivity().shortToast(R.string.error_invalid_email)
                false
            } else {
                true
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        when (registeredMode) {
            AppConstants.REGISTERED_MODE_PHONE -> {
                val phoneNumber = etPhoneNumber.text.toString()
                fabProceed.isEnabled = ValidationUtils.isPhoneNumberLengthValid(phoneNumber)
            }

            AppConstants.REGISTERED_MODE_EMAIL -> {
                val email = etEmail.text.toString()
                fabProceed.isEnabled = ValidationUtils.isEmailValid(email)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}