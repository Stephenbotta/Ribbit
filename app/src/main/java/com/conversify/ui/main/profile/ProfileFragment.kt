package com.conversify.ui.main.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.startLandingWithClear
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment() {
    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_profile

    private val viewModel by lazy { ViewModelProviders.of(this)[ProfileViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogout.setOnClickListener {
            if (isNetworkActiveWithMessage()) {
                viewModel.logout()
            }
        }
        observeChanges()
    }

    private fun observeChanges() {
        viewModel.logout.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    requireActivity().startLandingWithClear()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}