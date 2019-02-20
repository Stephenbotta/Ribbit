package com.conversify.ui.profile.settings.hideinfo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
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
        getBlockedUsers()
    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
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