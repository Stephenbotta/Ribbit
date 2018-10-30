package com.conversify.ui.loginsignup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_login_sign_up.*

class LoginSignUpActivity : BaseActivity(), BackButtonEnabledListener {
    companion object {
        private const val EXTRA_MODE = "EXTRA_MODE"
        private const val KEY_BACK_BUTTON_ENABLED = "KEY_BACK_BUTTON_ENABLED"

        fun start(context: Context, mode: Int) {
            context.startActivity(Intent(context, LoginSignUpActivity::class.java)
                    .putExtra(EXTRA_MODE, mode))
        }
    }

    private var isBackButtonEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sign_up)

        btnBack.setOnClickListener { onBackPressed() }

        if (savedInstanceState == null) {
            val mode = intent.getIntExtra(EXTRA_MODE, AppConstants.MODE_LOGIN)
            val fragment = LoginSignUpFragment.newInstance(mode)
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, fragment, LoginSignUpFragment.TAG)
                    .commit()
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