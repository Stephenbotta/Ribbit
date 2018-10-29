package com.conversify.ui.loginsignup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_login_sign_up.*

class LoginSignUpActivity : BaseActivity() {
    companion object {
        private const val EXTRA_MODE = "EXTRA_MODE"

        fun start(context: Context, mode: Int) {
            context.startActivity(Intent(context, LoginSignUpActivity::class.java)
                    .putExtra(EXTRA_MODE, mode))
        }
    }

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
        }
    }
}