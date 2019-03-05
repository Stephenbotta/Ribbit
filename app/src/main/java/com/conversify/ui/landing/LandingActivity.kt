package com.conversify.ui.landing

import android.content.Intent
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
import com.conversify.data.local.UserManager
import com.conversify.data.remote.PushType
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.clickSpannable
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.loginsignup.LoginSignUpActivity
import com.conversify.ui.main.MainActivity
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = UserManager.isLoggedIn()
        if (isLoggedIn) {
            val profile = UserManager.getProfile()
            when {
                profile.isProfileComplete == false -> {
                    LoginSignUpActivity.startWelcome(this)
                }

                profile.isInterestSelected == false -> {
                    LoginSignUpActivity.startChooseInterests(this)
                }

                profile.isVerified == true && profile.isPasswordExist == true -> {
                    val type = intent.getStringExtra("TYPE")
                    val intent = Intent(this, MainActivity::class.java)
                    if (!type.isNullOrEmpty()) {
                        intent.putExtra("TYPE", type)
                        intent.putExtra("id", intent.getStringExtra("id"))
                        when (type) {
                            PushType.CHAT -> {
                                val data = intent.getParcelableExtra<ProfileDto>("data")
                                intent.putExtra("data", data)
                            }
                            PushType.GROUP_CHAT -> {
                                val data = intent.getParcelableExtra<GroupDto>("data")
                                intent.putExtra("data", data)
                            }
                            PushType.VENUE_CHAT -> {
                                val data = intent.getParcelableExtra<VenueDto>("data")
                                intent.putExtra("data", data)
                            }
                            else -> {

                            }
                        }
                    }
                    startActivity(intent)
                    finishAffinity()
                }

                else -> {
                    setTheme(R.style.AppTheme_Landing)
                    setContentView(R.layout.activity_landing)
                    setListeners()
                }
            }
        } else {
            setTheme(R.style.AppTheme_Landing)
            setContentView(R.layout.activity_landing)
            setListeners()
        }
    }

    private fun setListeners() {
        btnGetStarted.setOnClickListener {
            LoginSignUpActivity.start(this, AppConstants.MODE_SIGN_UP)
        }

        val boldTypeface = ResourcesCompat.getFont(this, R.font.roboto_text_bold)
        val clickableTextColor = R.color.white

        val textSignIn = getString(R.string.login_sign_up_btn_sign_in)
        tvLabelAlreadyAMember.clickSpannable(spannableText = textSignIn,
                textTypeface = boldTypeface,
                textColorRes = clickableTextColor,
                clickListener = View.OnClickListener {
                    LoginSignUpActivity.start(this, AppConstants.MODE_LOGIN)
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
                textPaint.color = ContextCompat.getColor(this@LandingActivity, clickableTextColor)
                textPaint.typeface = boldTypeface
            }

            override fun onClick(widget: View) {
            }
        }

        val privacyClickable = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = false
                textPaint.color = ContextCompat.getColor(this@LandingActivity, clickableTextColor)
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