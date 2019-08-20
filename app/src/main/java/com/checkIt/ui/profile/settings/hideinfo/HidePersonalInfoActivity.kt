package com.checkIt.ui.profile.settings.hideinfo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.checkIt.R
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.ui.base.BaseActivity
import com.checkIt.ui.profile.settings.hideinfo.hidestatus.HideStatusActivity
import com.checkIt.utils.AppConstants
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

        setData(viewModel.getProfile())
        setListener()
        observeChanges()
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

        /*if (profile.isAccountPrivate == true)
            privateAccount.isChecked = profile.isAccountPrivate
        else*/ privateAccount.isChecked = profile.isAccountPrivate ?: false

        if (profile.imageVisibilityForEveryone == true) {
            tvPicViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            val size = profile.imageVisibility?.size
            if (profile.imageVisibilityForFollowers == false) {
                /* if (size != 0) {
                     tvPicViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                 } else {*/
                tvPicViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                /*}*/
            } else {
                tvPicViewStatus.text = getString(R.string.hide_info_my_followers)
            }
        }

        if (profile.personalInfoVisibilityForFollowers == false) {
            val size = profile.personalInfoVisibility?.size
            /*if (size != 0) {
                tvInfoViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
            } else {*/
            tvInfoViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
            /*}*/
        } else {
            tvInfoViewStatus.text = getString(R.string.hide_info_my_followers)
        }

        if (profile.nameVisibilityForEveryone == true) {
            tvUsernameViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            val size = profile.nameVisibility?.size
            if (profile.nameVisibilityForFollowers == false) {
                /*if (size != 0) {
                    tvUsernameViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                } else {*/
                tvUsernameViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                /* }*/
            } else {
                tvUsernameViewStatus.text = getString(R.string.hide_info_my_followers)
            }
        }

        if (profile.tagPermissionForEveryone == true) {
            tvMessageViewStatus.text = getString(R.string.hide_info_everyone)
        } else {
            val size = profile.tagPermission?.size
            if (profile.tagPermissionForFollowers == false) {
                /* if (size != 0) {
                     tvMessageViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                 } else {*/
                tvMessageViewStatus.text = getString(R.string.hide_info_label_status, size, getString(R.string.hide_info_people))
                /*}*/
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
                    val data = resource.data ?: ProfileDto()
                    /*if (data?.isAccountPrivate == true)
                        privateAccount.isChecked = data.isAccountPrivate
                    else*/ /*privateAccount.isChecked = data?.isAccountPrivate ?: false*/
                    setData(data)
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
        viewModel.privateAccount(viewModel.getProfile().isAccountPrivate?.not() ?: false)
    }

    private fun status(flag: Int) {
        val intent = HideStatusActivity.start(this, flag)
        startActivityForResult(intent, AppConstants.REQ_CODE_HIDE_INFO_STATUS)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.privateAccount, R.id.tvLabelPrivateMsg -> isAccountPrivate()

            R.id.tvProfilePic, R.id.tvPicViewStatus, R.id.tvLabelPicMsg -> status(ApiConstants.FLAG_PROFILE_PICTURE)

            R.id.tvInfo, R.id.tvInfoViewStatus, R.id.tvLabelInfoMsg -> status(ApiConstants.FLAG_PRIVATE_INFO)

            R.id.tvUsername, R.id.tvUsernameViewStatus, R.id.tvLabelUsernameMsg -> status(ApiConstants.FLAG_USERNAME)

            R.id.tvMessage, R.id.tvMessageViewStatus, R.id.tvLabelMessage -> status(ApiConstants.FLAG_MESSAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_HIDE_INFO_STATUS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val profile = data.getParcelableExtra<ProfileDto>(AppConstants.EXTRA_PROFILE)
                    if (profile != null) {
                        setData(profile)
                    }
                }
            }
        }
    }

}