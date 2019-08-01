package com.pulse.ui.loginsignup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.pulse.R
import com.pulse.data.local.UserManager
import com.pulse.extensions.gone
import com.pulse.extensions.hideKeyboard
import com.pulse.extensions.visible
import com.pulse.ui.base.BaseActivity
import com.pulse.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.pulse.ui.loginsignup.login.LoginFragment
import com.pulse.ui.loginsignup.signup.SignUpFragment
import com.pulse.ui.loginsignup.welcome.WelcomeFragment
import com.pulse.utils.AppConstants
import kotlinx.android.synthetic.main.activity_login_sign_up.*
import timber.log.Timber

class LoginSignUpActivity : BaseActivity(), BackButtonEnabledListener {
    companion object {
        private const val EXTRA_MODE = "EXTRA_MODE"
        private const val EXTRA_NAVIGATE_SCREEN_TAG = "EXTRA_NAVIGATE_SCREEN_TAG"
        private const val KEY_BACK_BUTTON_ENABLED = "KEY_BACK_BUTTON_ENABLED"

        /**
         * @param mode Should be either AppConstants.MODE_LOGIN or AppConstants.MODE_SIGN_UP
         * */
        fun start(context: Context, mode: Int) {
            context.startActivity(Intent(context, LoginSignUpActivity::class.java)
                    .putExtra(EXTRA_MODE, mode))
        }

        fun startWelcome(context: Context) {
            context.startActivity(Intent(context, LoginSignUpActivity::class.java)
                    .putExtra(EXTRA_NAVIGATE_SCREEN_TAG, WelcomeFragment.TAG))
        }

        fun startChooseInterests(context: Context) {
            context.startActivity(Intent(context, LoginSignUpActivity::class.java)
                    .putExtra(EXTRA_NAVIGATE_SCREEN_TAG, ChooseInterestsFragment.TAG))
        }
    }

    private var isBackButtonEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sign_up)

        btnBack.setOnClickListener {
            it.hideKeyboard()
            onBackPressed()
        }

        if (savedInstanceState == null) {
            val navigateScreenTag = intent.getStringExtra(EXTRA_NAVIGATE_SCREEN_TAG)

            Timber.i("Navigate screen tag : $navigateScreenTag")
            when (navigateScreenTag) {
                WelcomeFragment.TAG -> {
                    val fragment = WelcomeFragment.newInstance(UserManager.getProfile())
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.flContainer, fragment, WelcomeFragment.TAG)
                            .addToBackStack(null)
                            .commit()
                }

                ChooseInterestsFragment.TAG -> {
                    val fragment = ChooseInterestsFragment.newInstance(interest = UserManager.getProfile().interests
                            ?: arrayListOf())
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.flContainer, fragment, ChooseInterestsFragment.TAG)
                            .commit()
                }

                else -> {
                    val mode = intent.getIntExtra(EXTRA_MODE, AppConstants.MODE_LOGIN)
                    when (mode) {
                        AppConstants.MODE_LOGIN -> {
                            val fragment = LoginFragment.newInstance()
                            supportFragmentManager.beginTransaction()
                                    .add(R.id.flContainer, fragment, LoginFragment.TAG)
                                    .commit()
                        }

                        else -> {
                            val fragment = SignUpFragment.newInstance()
                            supportFragmentManager.beginTransaction()
                                    .add(R.id.flContainer, fragment, SignUpFragment.TAG)
                                    .commit()
                        }
                    }
                }
            }
        } else {
            // Restore state of back button
            val isEnabled = savedInstanceState.getBoolean(KEY_BACK_BUTTON_ENABLED, true)
            onBackButtonEnabled(isEnabled)
        }
    }

    override fun onBackButtonEnabled(isEnabled: Boolean) {
        isBackButtonEnabled = isEnabled
        if (isEnabled) {
            btnBack.visible()
        } else {
            btnBack.gone()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        // Save state of back button
        outState?.putBoolean(KEY_BACK_BUTTON_ENABLED, isBackButtonEnabled)
    }

    override fun onBackPressed() {
        if (isBackButtonEnabled) {
            super.onBackPressed()
        }
    }
}