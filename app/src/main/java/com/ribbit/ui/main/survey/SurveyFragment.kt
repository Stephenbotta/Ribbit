package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.survey.SurveyInfo
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_survey.*


class SurveyFragment : BaseFragment(),SurveyAdapter.Callback {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): SurveyFragment {
            val profileFragment = SurveyFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    private val fromTab by lazy { arguments?.getBoolean(ARGUMENT_FROM_TAB) ?: true }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var interestsAdapter: SurveyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        viewModel.getSurveyList()
        observeChanges()
    }



    private fun observeChanges() {
        viewModel.surveyList.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                 //   visible()
                    setupSurveyRecycler(resource.data?.info)
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                    // Ignored
                }
            }
        })
    }

    private fun setupSurveyRecycler(info: List<SurveyInfo>?) {

        interestsAdapter = SurveyAdapter(this)

        if (info != null) {
            interestsAdapter.displayInterests(info)
        }

        val layoutManager = FlexboxLayoutManager(requireActivity())
        layoutManager.flexWrap = FlexWrap.WRAP
        llSpinners.layoutManager = layoutManager
        llSpinners.isNestedScrollingEnabled = false
        llSpinners.adapter = interestsAdapter
    }


    override fun onEditInterestsClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}