package com.conversify.ui.people

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.databinding.FragmentPeopleBinding
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog

class PeopleFragment : BaseFragment() {

    private lateinit var viewModel: PeopleViewModel
    private lateinit var binding: FragmentPeopleBinding
    private lateinit var loadingDialog: LoadingDialog

    companion object {
        const val TAG = "PeopleFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_people

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, getFragmentLayoutResId(), container, false)
        binding.view = this
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(PeopleViewModel::class.java)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        loadingDialog = LoadingDialog(requireContext())
        observeChanges()
        callApi()

    }

    private fun callApi() {
        if (isNetworkActiveWithMessage()) {
            loadingDialog.setLoading(true)
            viewModel.getCrossedPeople()
        }
    }

    private fun observeChanges() {

        viewModel.crossedPeople.observe(this, Observer { resource ->
            loadingDialog.setLoading(false)
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data
                    activity?.shortToast(data?.firstOrNull()?.locationName.toString())
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {

                }
            }

        })
    }
}