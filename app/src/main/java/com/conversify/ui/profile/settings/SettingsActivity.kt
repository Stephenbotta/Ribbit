package com.conversify.ui.profile.settings

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.startLandingWithClear
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by lazy { ViewModelProviders.of(this)[SettingsViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        loadingDialog = LoadingDialog(this)
//        setData(viewModel.getProfile())
        setListener()
        observeChanges()
    }

    private fun setData(profile: ProfileDto) {

    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvVerification.setOnClickListener(this)
        tvInvitePeople.setOnClickListener(this)
        tvShareContactDetails.setOnClickListener(this)
        tvHidePersonalInfo.setOnClickListener(this)
        tvBlockUsers.setOnClickListener(this)
        tvAccessLocation.setOnClickListener(this)
        tvContactUs.setOnClickListener(this)
        tvTermsAndConditions.setOnClickListener(this)
        tvPush.setOnClickListener(this)
        tvAlert.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
    }

    private fun observeChanges() {

        viewModel.logout.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    startLandingWithClear()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })

        viewModel.editProfile.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    if (resource.error != AppError.WaitingForNetwork) {
                        handleError(resource.error)
                    }
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.profile_message_confirm_logout)
                .setPositiveButton(R.string.profile_btn_logout) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.logout()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.tvVerification -> {
            }

            R.id.tvInvitePeople -> {
            }

            R.id.tvShareContactDetails -> {
            }

            R.id.tvHidePersonalInfo -> {
            }

            R.id.tvBlockUsers -> {
            }

            R.id.tvAccessLocation -> {
            }

            R.id.tvContactUs -> {
            }

            R.id.tvTermsAndConditions -> {
            }

            R.id.tvPush -> {
            }

            R.id.tvAlert -> {
            }

            R.id.tvLogout -> showLogoutConfirmationDialog()
        }
    }
}