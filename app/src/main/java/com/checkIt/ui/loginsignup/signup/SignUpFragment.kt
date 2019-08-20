package com.checkIt.ui.loginsignup.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.checkIt.R
import com.checkIt.data.local.PrefsManager
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.FacebookLogin
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.loginsignup.SignUpRequest
import com.checkIt.data.remote.models.social.FacebookProfile
import com.checkIt.data.remote.models.social.SocialProfile
import com.checkIt.extensions.*
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.loginsignup.LoginSignUpViewModel
import com.checkIt.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.checkIt.ui.loginsignup.createpassword.CreatePasswordFragment
import com.checkIt.ui.loginsignup.loginpassword.LoginPasswordFragment
import com.checkIt.ui.loginsignup.verification.VerificationFragment
import com.checkIt.ui.loginsignup.welcome.WelcomeFragment
import com.checkIt.ui.main.MainActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.SocialUtils
import com.checkIt.utils.ValidationUtils
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.fragment_sign_up.*
import timber.log.Timber

class SignUpFragment : BaseFragment(), TextWatcher, FacebookLogin.FacebookLoginListener {
    companion object {
        const val TAG = "SignUpFragment"

        fun newInstance(): Fragment {
            val fragment = SignUpFragment()
            val arguments = Bundle()
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: LoginSignUpViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var facebookLogin: FacebookLogin
    private lateinit var googleSignInClient: GoogleSignInClient
    private var registeredMode = AppConstants.REGISTERED_MODE_PHONE

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_sign_up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[LoginSignUpViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        facebookLogin = FacebookLogin(this)
        setupGoogleSignInClient()
        setupViews()
        setListeners()
        observeChanges()
    }

    override fun onStart() {
        super.onStart()
        registeredMode = AppConstants.REGISTERED_MODE_PHONE
    }

    private fun setupGoogleSignInClient() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), options)
    }

    private fun setupViews() {
        fabProceed.isEnabled = false
        countryCodePicker.setTypeFace(ResourcesCompat.getFont(requireActivity(), R.font.roboto_text_medium))
        countryCodePicker.registerCarrierNumberEditText(etPhoneNumber)
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

        cvContinueWithFacebook.setOnClickListener {
            facebookLogin.performLogin(this)
        }

        cvContinueWithGoogle.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, AppConstants.REQ_CODE_GOOGLE_SIGN_IN)
        }

        fabProceed.setOnClickListener {
            if (formDataValid() && requireActivity().isNetworkActiveWithMessage()) {
                etPhoneNumber.clearFocus()
                etEmail.clearFocus()
                it.hideKeyboard()

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

    private fun observeChanges() {
        viewModel.loginRegister.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val profile = resource.data
                    if (profile != null) {
                        handleNavigation(profile)
                    } else {
                        Timber.w("Received profile is null")
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
        return if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
            // Validations for phone number
            val phoneNumber = etPhoneNumber.text.toString()

            if (phoneNumber.isEmpty()) {
                requireActivity().shortToast(R.string.error_empty_phone_number)
                false
            } else if (!isPhoneNumberValid()) {
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

    private fun isPhoneNumberValid(): Boolean {
        var isNumberValid = false
        countryCodePicker.setPhoneNumberValidityChangeListener { isValid ->
            isNumberValid = isValid
        }
        return isNumberValid
    }

    private fun handleNavigation(profile: ProfileDto) {
        if (profile.isVerified == true) {
            if (profile.isPasswordExist == true) {
                if (profile.isProfileComplete == true) {
                    val isSocialProfile = !profile.googleId.isNullOrBlank() ||
                            !profile.facebookId.isNullOrBlank()
                    if (isSocialProfile) {
                        if (profile.isInterestSelected == true) {
                            startActivity(Intent(requireActivity(), MainActivity::class.java))
                            requireActivity().finishAffinity()
                        } else {
                            // When interests are not selected for social profile
                            val fragment = ChooseInterestsFragment.newInstance(interest = arrayListOf())
                            val tag = ChooseInterestsFragment.TAG
                            navigateToFragment(fragment, tag, true, false)
                        }
                    } else {
                        // Password screen for login
                        val credentials = etEmail.text.toString()
                        val fragment = LoginPasswordFragment.newInstance(profile, credentials)
                        val tag = LoginPasswordFragment.TAG
                        navigateToFragment(fragment, tag)
                    }
                } else {
                    // Welcome screen to fill user details. In case of social login.
                    val fragment = WelcomeFragment.newInstance(profile)
                    val tag = WelcomeFragment.TAG
                    navigateToFragment(fragment, tag, false)
                }
            } else {
                // Create Password
                val fragment = CreatePasswordFragment.newInstance(profile)
                val tag = CreatePasswordFragment.TAG
                navigateToFragment(fragment, tag)
            }
        } else {
            // OTP Verification
            PrefsManager.get().save("countryCode", profile.countryCode ?: "")
            val fragment = VerificationFragment.newInstance(profile)
            val tag = VerificationFragment.TAG
            navigateToFragment(fragment, tag)
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String, replace: Boolean = false, backStack: Boolean = true) {
        fragmentManager?.apply {
            val transaction = beginTransaction()
            transaction.setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                    R.anim.parallax_left_in, R.anim.parallax_right_out)
            if (replace) {
                transaction.replace(R.id.flContainer, fragment, tag)
            } else {
                transaction.add(R.id.flContainer, fragment, tag)
            }
            if (backStack) {
                transaction.addToBackStack(null)
            }
            transaction.commit()
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

    override fun onFacebookLoginSuccess() {
        Timber.d("Facebook login success")
        loadingDialog.setLoading(true)
        facebookLogin.getUserProfile()
    }

    override fun onFacebookLoginCancel() {
        Timber.d("Facebook login cancel")
        loadingDialog.setLoading(false)
    }

    override fun onFacebookLoginError(exception: FacebookException) {
        Timber.d(exception)
        loadingDialog.setLoading(false)
    }

    override fun onFacebookProfileSuccess(profile: FacebookProfile) {
        loadingDialog.setLoading(false)
        val socialProfile = SocialUtils.getFacebookSocialProfile(profile)
        Timber.d("Facebook Profile : %s", socialProfile.toString())
        handleSocialProfileSuccess(socialProfile)
    }

    private fun handleSocialProfileSuccess(socialProfile: SocialProfile) {
        if (!isNetworkActiveWithMessage()) return

        val request = if (socialProfile.source == ApiConstants.FLAG_REGISTER_FACEBOOK) {
            SignUpRequest(flag = ApiConstants.FLAG_REGISTER_FACEBOOK,
                    facebookId = socialProfile.socialId,
                    email = socialProfile.email,
                    fullName = socialProfile.fullName)
        } else {
            SignUpRequest(flag = ApiConstants.FLAG_REGISTER_GOOGLE,
                    googleId = socialProfile.socialId,
                    email = socialProfile.email,
                    fullName = socialProfile.fullName)
        }
        viewModel.signUp(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppConstants.REQ_CODE_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val result = task.getResult(ApiException::class.java)
                if (result != null) {
                    val socialProfile = SocialUtils.getGoogleSocialProfile(result)
                    Timber.d("Google Profile : %s", socialProfile.toString())
                    handleSocialProfileSuccess(socialProfile)
                } else {
                    Timber.i("Google sign in result is null")
                }
            } catch (exception: Exception) {
                Timber.w(exception)
            }
            googleSignInClient.signOut()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            facebookLogin.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        facebookLogin.unregisterCallback()
        loadingDialog.setLoading(false)
    }
}