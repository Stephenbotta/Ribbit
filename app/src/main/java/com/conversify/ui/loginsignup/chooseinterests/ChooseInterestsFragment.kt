package com.conversify.ui.loginsignup.chooseinterests

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.shortToast
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.BackButtonEnabledListener
import com.conversify.ui.main.MainActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_choose_interests.*

class ChooseInterestsFragment : BaseFragment() {
    companion object {
        private const val ARGUMENT_STARTED_FOR_RESULT = "ARGUMENT_STARTED_FOR_RESULT"
        const val TAG = "ChooseInterestsFragment"

        private const val CHILD_INTERESTS = 0
        private const val CHILD_LOADING = 1
        private const val CHILD_RETRY = 2

        private const val MINIMUM_INTEREST_COUNT = 3

        fun newInstance(startedForResult: Boolean = false): Fragment {
            val fragment = ChooseInterestsFragment()
            val arguments = Bundle()
            arguments.putBoolean(ARGUMENT_STARTED_FOR_RESULT, startedForResult)
            fragment.arguments = arguments
            return fragment
        }
    }

    private val startedForResult: Boolean by lazy {
        arguments?.getBoolean(ARGUMENT_STARTED_FOR_RESULT) ?: false
    }
    private lateinit var viewModel: ChooseInterestsViewModel
    private lateinit var loadingDialog: LoadingDialog
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
        loadingDialog = LoadingDialog(requireActivity())

        interestsAdapter = ChooseInterestsAdapter(GlideApp.with(this))
        rvInterests.adapter = interestsAdapter

        setListeners()
        observeChanges()
        getInterests()
    }

    private fun setListeners() {
        btnRetry.setOnClickListener {
            getInterests()
        }

        btnContinue.setOnClickListener {
            val selectedInterests = interestsAdapter.getSelectedInterests()
            if (selectedInterests.size < MINIMUM_INTEREST_COUNT) {
                requireActivity().shortToast(R.string.choose_interests_message_choose_at_least_3_interests)
            } else if (isNetworkActiveWithMessage()) {
                viewModel.updateInterests(selectedInterests)
            }
        }
    }

    private fun observeChanges() {
        viewModel.interests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    viewFlipper.displayedChild = CHILD_INTERESTS
                    val interests = resource.data ?: emptyList()

                    // If started for result then set my interests as selected
                    if (startedForResult) {
                        // Form a set of my interest ids
                        val myInterestIds = viewModel.myInterests.mapNotNull { it.id }.toSet()

                        // Set all matched interests to selected
                        interests.forEach { interest ->
                            if (myInterestIds.contains(interest.id)) {
                                interest.selected = true
                            }
                        }
                    }
                    interestsAdapter.displayInterests(interests)
                    if (interests.isNotEmpty()) {
                        btnContinue.visible()
                    }
                }

                Status.ERROR -> {
                    viewFlipper.displayedChild = CHILD_RETRY
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> viewFlipper.displayedChild = CHILD_LOADING
            }
        })

        viewModel.updateInterests.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    if (startedForResult) {
                        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
                        requireActivity().onBackPressed()
                    } else {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finishAffinity()
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    private fun getInterests() {
        val shouldFetchInterests = viewModel.hasInterests() || isNetworkActiveWithMessage()
        if (shouldFetchInterests) {
            viewModel.getInterests()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}