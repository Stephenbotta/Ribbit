package com.ribbit.ui.loginsignup.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.FacebookLogin
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.LoginRequest
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.social.FacebookProfile
import com.ribbit.data.remote.models.social.SocialProfile
import com.ribbit.extensions.handleError
import com.ribbit.extensions.hideKeyboard
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.updateAlphaLevel
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.loginsignup.LoginSignUpViewModel
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.ribbit.ui.loginsignup.createpassword.CreatePasswordFragment
import com.ribbit.ui.loginsignup.loginpassword.LoginPasswordFragment
import com.ribbit.ui.loginsignup.verification.VerificationFragment
import com.ribbit.ui.loginsignup.welcome.WelcomeFragment
import com.ribbit.ui.main.MainActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.SocialUtils
import kotlinx.android.synthetic.main.fragment_login.*
import timber.log.Timber

class LoginFragment : BaseFragment(), TextWatcher, FacebookLogin.FacebookLoginListener {
    companion object {
        const val TAG = "LoginFragment"

        fun newInstance(): Fragment {
            val fragment = LoginFragment()
            val arguments = Bundle()
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: LoginSignUpViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var facebookLogin: FacebookLogin
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_login

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

    private fun setupGoogleSignInClient() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), options)
    }

    private fun setupViews() {
        fabProceed.isEnabled = false
        fabProceed.updateAlphaLevel()
    }

    private fun setListeners() {
        etCredentials.addTextChangedListener(this)

        cvContinueWithFacebook.setOnClickListener {
            facebookLogin.performLogin(this)
        }

        cvContinueWithGoogle.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, AppConstants.REQ_CODE_GOOGLE_SIGN_IN)
        }

        fabProceed.setOnClickListener {
            if (formDataValid() && requireActivity().isNetworkActiveWithMessage()) {
                etCredentials.clearFocus()
                it.hideKeyboard()

                val credentials = etCredentials.text.toString().trim()
                val request = LoginRequest(credentials = credentials)
                viewModel.login(request)
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
        return etCredentials.text.toString().trim().isNotBlank()
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
                            val fragment = ChooseInterestsFragment.newInstance(interest = profile.interests
                                    ?: ArrayList())
                            val tag = ChooseInterestsFragment.TAG
                            navigateToFragment(fragment, tag, true, false)
                        }
                    } else {
                        // Password screen for login
                        val credentials = etCredentials.text.toString()
                        val fragment = LoginPasswordFragment.newInstance(profile, credentials)
                        val tag = LoginPasswordFragment.TAG
                        navigateToFragment(fragment, tag)
                    }
                } else {
                    // Welcome screen to fill user details. In case of social login.
                    val fragment = WelcomeFragment.newInstance(profile)
                    val tag = WelcomeFragment.TAG
                    navigateToFragment(fragment, tag, false, true)
                }
            } else {
                // Create Password
                val fragment = CreatePasswordFragment.newInstance(profile)
                val tag = CreatePasswordFragment.TAG
                navigateToFragment(fragment, tag)
            }
        } else {
            // OTP Verification
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
        fabProceed.isEnabled = formDataValid()
        fabProceed.updateAlphaLevel()
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

        // Only sent if it exists
        val email = if (socialProfile.email.isBlank()) {
            null
        } else {
            socialProfile.email
        }
        val request = if (socialProfile.source == ApiConstants.FLAG_REGISTER_FACEBOOK) {
            LoginRequest(facebookId = socialProfile.socialId,
                    email = email)
        } else {
            LoginRequest(googleId = socialProfile.socialId,
                    email = email)
        }
        viewModel.login(request)
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