package com.conversify.ui.profile.settings.hideinfo.hidestatus

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
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_hide_status.*

class HideStatusActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        fun start(context: Context, flag: Int): Intent {
            return Intent(context, HideStatusActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[HideStatusViewModel::class.java] }
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide_status)

        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        setData(viewModel.getProfile(), flag)
        setListener()
        observeChanges()
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
    }

    private fun setData(profile: ProfileDto, flag: Int) {

        when (flag) {
            ApiConstants.FLAG_PROFILE_PICTURE -> {

                if (profile.imageVisibilityForEveryone!!) {
                    everyone.isChecked = profile.imageVisibilityForEveryone
                } else {
                    if (profile.imageVisibilityForFollowers!!) {
                        val size = profile.imageVisibility?.size
                        if (size != 0) {
                            selectedUser.isChecked = true
                        } else {
                            yourFollowers.isChecked = profile.imageVisibilityForFollowers
                        }
                    } else {
                        yourFollowers.isChecked = true
                    }
                }


            }
            ApiConstants.FLAG_PRIVATE_INFO -> {
            }
            ApiConstants.FLAG_USERNAME -> {
            }
            ApiConstants.FLAG_MESSAGE -> {
            }
        }

    }

    private fun observeChanges() {

        viewModel.blockUsersList.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {

                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })
    }

    private fun getBlockedUsers() {
        if (isNetworkActiveWithMessage())
            viewModel.getBlockedUsers()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

        }
    }

}