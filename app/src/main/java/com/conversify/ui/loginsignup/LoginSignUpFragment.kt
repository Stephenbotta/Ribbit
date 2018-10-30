package com.conversify.ui.loginsignup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.conversify.R
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseFragment
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

    private var mode = AppConstants.MODE_LOGIN
    private var registeredMode = AppConstants.REGISTERED_MODE_PHONE

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login_sign_up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mode = arguments?.getInt(ARGUMENT_MODE) ?: AppConstants.MODE_LOGIN

        if (mode == AppConstants.MODE_LOGIN) {
            tvLabelTitle.setText(R.string.login)
        } else {
            tvLabelTitle.setText(R.string.sign_up)
        }

        setListeners()
        fabProceed.isEnabled = false
    }

    private fun setListeners() {
        val selectedTextColor = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)
        val deSelectedTextColor = ContextCompat.getColor(requireActivity(), R.color.textGray)

        btnPhoneNumber.setOnClickListener {
            fabProceed.isEnabled = false
            registeredMode = AppConstants.REGISTERED_MODE_PHONE
            btnPhoneNumber.setTextColor(selectedTextColor)
            btnEmail.setTextColor(deSelectedTextColor)
            countryCodePicker.visible()
            etPhoneNumber.visible()
            etPhoneNumber.requestFocus()
            etEmail.setText("")
            etEmail.gone()
        }

        btnEmail.setOnClickListener {
            fabProceed.isEnabled = false
            registeredMode = AppConstants.REGISTERED_MODE_EMAIL
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
            val fragment: Fragment
            val fragmentTag: String
            if (mode == AppConstants.MODE_LOGIN) {
                fragment = LoginPasswordFragment.newInstance(registeredMode)
                fragmentTag = LoginPasswordFragment.TAG
            } else {
                fragment = VerificationFragment.newInstance(registeredMode)
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
}