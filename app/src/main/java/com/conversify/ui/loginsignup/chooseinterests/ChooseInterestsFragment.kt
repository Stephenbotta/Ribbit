package com.conversify.ui.loginsignup.chooseinterests

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.BackButtonEnabledListener
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_choose_interests.*

class ChooseInterestsFragment : BaseFragment() {
    companion object {
        const val TAG = "ChooseInterestsFragment"

        private const val CHILD_INTERESTS = 0
        private const val CHILD_LOADING = 1

        private const val MINIMUM_INTEREST_COUNT = 3
    }

    private lateinit var viewModel: ChooseInterestsViewModel
    private lateinit var interestsAdapter: ChooseInterestsAdapter
    private var backButtonEnabledListener: BackButtonEnabledListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BackButtonEnabledListener) {
            backButtonEnabledListener = context
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_choose_interests

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButtonEnabledListener?.onBackButtonEnabled(false)
        viewModel = ViewModelProviders.of(this)[ChooseInterestsViewModel::class.java]

        interestsAdapter = ChooseInterestsAdapter(GlideApp.with(this))
        rvInterests.adapter = interestsAdapter

        btnContinue.setOnClickListener {

        }

        viewModel.interests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    viewFlipper.displayedChild = CHILD_INTERESTS
                    val interests = resource.data ?: emptyList()
                    interestsAdapter.displayInterests(interests)
                    if (interests.isNotEmpty()) {
                        btnContinue.visible()
                    }
                }

                Status.ERROR -> {
                    viewFlipper.displayedChild = CHILD_INTERESTS
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> viewFlipper.displayedChild = CHILD_LOADING
            }
        })

        if (requireActivity().isNetworkActiveWithMessage()) {
            viewModel.getInterests()
        }
    }
}