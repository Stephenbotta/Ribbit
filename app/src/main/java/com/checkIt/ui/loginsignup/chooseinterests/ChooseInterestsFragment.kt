package com.checkIt.ui.loginsignup.chooseinterests

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.extensions.handleError
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.loginsignup.BackButtonEnabledListener
import com.checkIt.ui.main.MainActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_choose_interests.*

class ChooseInterestsFragment : BaseFragment(), ChooseInterestsAdapter.Callback {
    companion object {
        private const val ARGUMENT_STARTED_FOR_RESULT = "ARGUMENT_STARTED_FOR_RESULT"
        private const val ARGUMENT_UPDATE_PREF = "ARGUMENT_UPDATE_PREF"
        private const val ARGUMENT_EXTRA_LIST = "ARGUMENT_EXTRA_LIST"
        private const val ARGUMENT_EXTRA_COUNT = "ARGUMENT_EXTRA_COUNT"
        const val TAG = "ChooseInterestsFragment"

        private const val CHILD_INTERESTS = 0
        private const val CHILD_LOADING = 1
        private const val CHILD_RETRY = 2

        private const val MINIMUM_INTEREST_COUNT = 3

        fun newInstance(startedForResult: Boolean = false, updateInPref: Boolean = true, interest: ArrayList<InterestDto>, count: Int = MINIMUM_INTEREST_COUNT): Fragment {
            val fragment = ChooseInterestsFragment()
            val arguments = Bundle()
            arguments.putBoolean(ARGUMENT_STARTED_FOR_RESULT, startedForResult)
            arguments.putBoolean(ARGUMENT_UPDATE_PREF, updateInPref)
            arguments.putParcelableArrayList(ARGUMENT_EXTRA_LIST, interest)
            arguments.putInt(ARGUMENT_EXTRA_COUNT, count)
            fragment.arguments = arguments
            return fragment
        }
    }

    private val startedForResult: Boolean by lazy {
        arguments?.getBoolean(ARGUMENT_STARTED_FOR_RESULT) ?: false
    }
    private val updateInPref: Boolean by lazy {
        arguments?.getBoolean(ARGUMENT_UPDATE_PREF) ?: false
    }
    private val interest: ArrayList<InterestDto> by lazy {
        arguments?.getParcelableArrayList(ARGUMENT_EXTRA_LIST) ?: ArrayList<InterestDto>()
    }
    private val count: Int by lazy {
        arguments?.getInt(ARGUMENT_EXTRA_COUNT) ?: MINIMUM_INTEREST_COUNT
    }
    private val selectedInterestIds by lazy { mutableListOf<String>() }
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
        viewModel.start(interest)
        loadingDialog = LoadingDialog(requireActivity())

        interestsAdapter = ChooseInterestsAdapter(GlideApp.with(this), this)
        rvInterests.adapter = interestsAdapter

        if (count == 1) {
            tvLabelChooseAtLeast.text = getString(R.string.choose_interests_message_choose_one_interests, count)
        } else {
            tvLabelChooseAtLeast.text = getString(R.string.choose_interests_message_choose_more_interests, count)
        }

        setListeners()
        observeChanges()
        getInterests()
    }

    private fun setListeners() {
        btnRetry.setOnClickListener {
            getInterests()
        }

        btnContinue.setOnClickListener {
            if (updateInPref) {
                if (isNetworkActiveWithMessage()) {
                    viewModel.updateInterests(selectedInterestIds.toList(), updateInPref)
                }
            } else {
                if (startedForResult) {
                    val selectedIdsIntent = Intent()
                    selectedIdsIntent.putParcelableArrayListExtra(AppConstants.EXTRA_INTEREST, interestsAdapter.getSelectedInterest())
                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, selectedIdsIntent)
                    requireActivity().onBackPressed()
                } else {
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finishAffinity()
                }
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
                        val myInterestIds = viewModel.myInterestIds

                        // Set all matched interests to selected
                        interests.forEach { interest ->
                            if (myInterestIds.contains(interest.id)) {
                                interest.selected = true
                                selectedInterestIds.add(interest.id
                                        ?: "")  // Add interest to selected set
                            }
                        }

                        // Update the continue button state
                        btnContinue.isEnabled = continueStatus()
//                        if (selectedInterestIds.size < count) {
//                            btnContinue.gone()
//                        } else {
//                            btnContinue.visible()
//                        }
                    }
                    interestsAdapter.displayInterests(interests)
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
                    val interests = ArrayList<InterestDto>()
                    interests.addAll(resource.data ?: emptyList())
                    if (startedForResult) {
                        val selectedIdsIntent = Intent()
                        selectedIdsIntent.putParcelableArrayListExtra(AppConstants.EXTRA_INTEREST, interests)
                        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, selectedIdsIntent)
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

    override fun onInterestClicked(interest: InterestDto) {
        val interestId = interest.id
        if (interestId != null) {
            if (interest.selected) {
                selectedInterestIds.add(interestId)
            } else {
                selectedInterestIds.remove(interestId)
            }
        }

        btnContinue.isEnabled = continueStatus()

    }

    private fun continueStatus(): Boolean {
        return if (selectedInterestIds.size < count) {
            return false
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}