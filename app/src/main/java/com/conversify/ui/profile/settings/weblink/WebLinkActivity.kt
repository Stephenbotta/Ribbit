package com.conversify.ui.profile.settings.weblink

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.conversify.R
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.AppConstants
import kotlinx.android.synthetic.main.activity_web_link.*

class WebLinkActivity : BaseActivity() {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        fun getStartIntent(context: Context, flag: Int): Intent {
            return Intent(context, WebLinkActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private val CONTACT_US_URL_CLIENT = "http://52.35.234.66:8001/contactUs"
    private val CONTACT_US_URL_DEV = "http://52.35.234.66:8000/contactUs"
    private val CONTACT_US_URL = CONTACT_US_URL_CLIENT

    private val TERMS_AND_CONDITIONS_URL_CLIENT = "http://52.35.234.66:8001/termsandcondition"
    private val TERMS_AND_CONDITIONS_URL_DEV = "http://52.35.234.66:8000/termsandcondition"
    private val TERMS_AND_CONDITIONS_URL = TERMS_AND_CONDITIONS_URL_CLIENT

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_link)
        inItClasses()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun inItClasses() {
        val flag = intent.getIntExtra(EXTRA_FLAG, 0)
        loadingDialog = LoadingDialog(this)

        wvWebLink.settings.javaScriptEnabled = true
        wvWebLink.settings.setAppCacheEnabled(true)
        wvWebLink.settings.domStorageEnabled = true
        wvWebLink.settings.builtInZoomControls = true
        wvWebLink.settings.displayZoomControls = false
        wvWebLink.settings.loadWithOverviewMode = true
        wvWebLink.settings.useWideViewPort = true
        wvWebLink.webChromeClient = WebChromeClient()
        when (flag) {
            AppConstants.REQ_CODE_CONTACT_US -> {
                btnBack.text = getString(R.string.settings_profile_label_contact_us)
                wvWebLink.loadUrl(CONTACT_US_URL)
            }
            AppConstants.REQ_CODE_TERMS_AND_CONDITIONS -> {
                btnBack.text = getString(R.string.settings_profile_label_terms_and_conditions)
                wvWebLink.loadUrl(TERMS_AND_CONDITIONS_URL)
            }
        }

        loadingDialog.setLoading(true)
        wvWebLink.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                //Make the bar disappear after URL is loaded
                if (progress == 100)
                    loadingDialog.setLoading(false)
            }
        }
        btnBack.setOnClickListener { onBackPressed() }
    }

}