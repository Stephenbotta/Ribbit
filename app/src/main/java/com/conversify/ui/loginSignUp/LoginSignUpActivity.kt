package com.conversify.ui.loginSignUp

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.conversify.R
import com.conversify.extensions.clickSpannable
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login_sign_up.*

class LoginSignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sign_up)

        setListeners()
    }

    private fun setListeners() {
        btnGetStarted.setOnClickListener { }

        val boldTypeface = ResourcesCompat.getFont(this, R.font.brandon_text_bold)
        val clickableTextColor = R.color.white

        val textSignIn = getString(R.string.login_sign_up_btn_sign_in)
        tvLabelAlreadyAMember.clickSpannable(spannableText = textSignIn,
                textTypeface = boldTypeface,
                textColorRes = clickableTextColor,
                clickListener = View.OnClickListener {
                })

        val textTermsAndConditions = getString(R.string.login_sign_up_label_terms_and_conditions)
        val textPrivacyPolicy = getString(R.string.login_sign_up_label_privacy_policy)

        val termsAndPrivacyFullText = tvLabelTermsAndPrivacy.text.toString()
        val termsAndPrivacySpannableString = SpannableString(termsAndPrivacyFullText)

        val termsStartIndex = termsAndPrivacyFullText.indexOf(textTermsAndConditions)
        val termsEndIndex = termsStartIndex + textTermsAndConditions.length

        val privacyStartIndex = termsAndPrivacyFullText.indexOf(textPrivacyPolicy)
        val privacyEndIndex = privacyStartIndex + textPrivacyPolicy.length

        val termsClickable = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = false
                textPaint.color = ContextCompat.getColor(this@LoginSignUpActivity, clickableTextColor)
                textPaint.typeface = boldTypeface
            }

            override fun onClick(widget: View) {
            }
        }

        val privacyClickable = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = false
                textPaint.color = ContextCompat.getColor(this@LoginSignUpActivity, clickableTextColor)
                textPaint.typeface = boldTypeface
            }

            override fun onClick(widget: View) {
            }
        }

        termsAndPrivacySpannableString.setSpan(termsClickable, termsStartIndex,
                termsEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        termsAndPrivacySpannableString.setSpan(privacyClickable, privacyStartIndex,
                privacyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvLabelTermsAndPrivacy.movementMethod = LinkMovementMethod.getInstance()
        tvLabelTermsAndPrivacy.highlightColor = Color.TRANSPARENT
        tvLabelTermsAndPrivacy.text = termsAndPrivacySpannableString
    }
}