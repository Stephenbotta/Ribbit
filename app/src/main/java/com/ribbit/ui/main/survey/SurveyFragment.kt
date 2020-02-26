package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.models.Status
import com.ribbit.extensions.handleError
import com.ribbit.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_survey.*

class SurveyFragment : BaseFragment() {
    companion object {
        const val TAG = "SurveyFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var adapter: SurveyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getSurveyList()
        observeChanges()

        adapter = SurveyAdapter(requireContext())
        llSpinners.adapter = adapter


        fabEdit.setOnClickListener {
            UserManager.saveDemographicClick(true)
            findNavController().navigate(R.id.action_surveyFragment_to_surveyDataFragment)
        }
    }

    private fun observeChanges() {
        viewModel.surveyList.observe(this, Observer { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    adapter.displaySurveys(resource.data?.info ?: emptyList())
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })
    }
}