package com.conversify.ui.loginSignUp

import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.conversify.R
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnBack.setOnClickListener { onBackPressed() }

        val selectedTextColor = ContextCompat.getColor(this, R.color.colorPrimary)
        val deSelectedTextColor = ContextCompat.getColor(this, R.color.textGray)

        btnPhoneNumber.setOnClickListener {
            btnPhoneNumber.setTextColor(selectedTextColor)
            btnEmail.setTextColor(deSelectedTextColor)
            countryCodePicker.visible()
            dividerPhoneNumber.visible()
            etPhoneNumber.visible()
            etPhoneNumber.requestFocus()
            etEmail.setText("")
            etEmail.gone()
        }

        btnEmail.setOnClickListener {
            btnEmail.setTextColor(selectedTextColor)
            btnPhoneNumber.setTextColor(deSelectedTextColor)
            countryCodePicker.gone()
            dividerPhoneNumber.gone()
            etEmail.visible()
            etEmail.requestFocus()
            etPhoneNumber.setText("")
            etPhoneNumber.gone()
        }

        cvContinueWithFacebook.setOnClickListener { }
        cvContinueWithGoogle.setOnClickListener { }
    }
}