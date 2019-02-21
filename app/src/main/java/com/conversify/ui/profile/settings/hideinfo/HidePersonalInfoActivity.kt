package com.conversify.ui.profile.settings.hideinfo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.profile.settings.hideinfo.hidestatus.HideStatusActivity
import kotlinx.android.synthetic.main.activity_hide_personal_info.*

class HidePersonalInfoActivity : BaseActivity(), View.OnClickListener {

    companion object {

        fun start(context: Context): Intent {
            return Intent(context, HidePersonalInfoActivity::class.java)
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[HidePersonalInfoViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide_personal_info)

        setListener()
        observeChanges()
    }

    override fun onStart() {
        super.onStart()
        setData(viewModel.getProfile())
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        privateAccount.setOnClickListener(this)
        tvLabelPrivateMsg.setOnClickListener(this)
        tvProfilePic.setOnClickListener(this)
        tvPicViewStatus.setOnClickListener(this)
        tvLabelPicMsg.setOnClickListener(this)
        tvInfo.setOnClickListener(this)
        tvInfoViewStatus.setOnClickListener(this)
        tvLabelInfoMsg.setOnClickListener(this)
        tvUsername.setOnClickListener(this)
        tvUsernameViewStatus.setOnClickListener(this)
        tvLabelUsernameMsg.setOnClickListener(this)
        tvMessage.setOnClickListener(this)
        tvMessageViewStatus.setOnClickListener(this)
        tvLabelMessage.setOnClickListener(this)
    }

    private fun setData(profile: ProfileDto) {

        if (profile.isPrivate!!)
            privateAccount.isChecked = profile.isPrivate
        else privateAccount.isChecked = profile.isPrivate

        if (profile.imageVisibilityForEveryone!!) {
            tvPicViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            if (profile.imageVisibilityForFollowers!!) {
                val size = profile.imageVisibility?.size
                if (size != 0) {
                    tvPicViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                } else {
                    tvPicViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                }
            } else {
                tvPicViewStatus.text = getString(R.string.hide_info_my_followers)
            }
        }

        if (profile.personalInfoVisibilityForFollowers!!) {
            val size = profile.personalInfoVisibility?.size
            if (size != 0) {
                tvInfoViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
            } else {
                tvInfoViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
            }
        } else {
            tvInfoViewStatus.text = getString(R.string.hide_info_my_followers)
        }

        if (profile.nameVisibilityForEveryone!!) {
            tvUsernameViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            if (profile.nameVisibilityForFollowers!!) {
                val size = profile.nameVisibility?.size
                if (size != 0) {
                    tvUsernameViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                } else {
                    tvUsernameViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                }
            } else {
                tvUsernameViewStatus.text = getString(R.string.hide_info_my_followers)
            }
        }

        if (profile.tagPermissionForEveryone!!) {
            tvMessageViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            if (profile.tagPermissionForFollowers!!) {
                val size = profile.tagPermission?.size
                if (size != 0) {
                    tvMessageViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                } else {
                    tvMessageViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                }
            } else {
                tvMessageViewStatus.text = getString(R.string.hide_info_my_followers)
            }
        }

    }

    private fun observeChanges() {

        viewModel.privateAccount.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data
                    if (data?.isPrivate!!)
                        privateAccount.isChecked = data.isPrivate
                    else privateAccount.isChecked = data.isPrivate
                }

                Status.ERROR -> {
//                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })
    }

    private fun isAccountPrivate() {
        viewModel.privateAccount(!viewModel.getProfile().isPrivate!!)
    }

    private fun status(flag: Int) {
        startActivity(HideStatusActivity.start(this, flag))
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.privateAccount -> isAccountPrivate()
            R.id.tvLabelPrivateMsg -> isAccountPrivate()

            R.id.tvProfilePic -> status(ApiConstants.FLAG_PROFILE_PICTURE)
            R.id.tvPicViewStatus -> status(ApiConstants.FLAG_PROFILE_PICTURE)
            R.id.tvLabelPicMsg -> status(ApiConstants.FLAG_PROFILE_PICTURE)

            R.id.tvInfo -> status(ApiConstants.FLAG_PRIVATE_INFO)
            R.id.tvInfoViewStatus -> status(ApiConstants.FLAG_PRIVATE_INFO)
            R.id.tvLabelInfoMsg -> status(ApiConstants.FLAG_PRIVATE_INFO)

            R.id.tvUsername -> status(ApiConstants.FLAG_USERNAME)
            R.id.tvUsernameViewStatus -> status(ApiConstants.FLAG_USERNAME)
            R.id.tvLabelUsernameMsg -> status(ApiConstants.FLAG_USERNAME)

            R.id.tvMessage -> status(ApiConstants.FLAG_MESSAGE)
            R.id.tvMessageViewStatus -> status(ApiConstants.FLAG_MESSAGE)
            R.id.tvLabelMessage -> status(ApiConstants.FLAG_MESSAGE)

        }
    }

}