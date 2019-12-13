package com.ribbit.ui.main.survey

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.survey.GetSurveyProperties
import com.ribbit.extensions.handleError
import com.ribbit.extensions.setArrayAdapter
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.custom_spinner_layout.view.*
import kotlinx.android.synthetic.main.fragment_survey_data.*

class SurveyDataFragment : BaseFragment() {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): SurveyDataFragment {
            val profileFragment = SurveyDataFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }
    private lateinit var loadingDialog: LoadingDialog
    private val fromTab by lazy { arguments?.getBoolean(ARGUMENT_FROM_TAB) ?: true }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_data

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var interestsAdapter: SurveyAdapter


    val list = mutableListOf<String>("Male", "Gender", "Female")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(context!!)
        //  spGender?.setArrayAdapter(list)

     //   initChilds()
        viewModel.getUserProfileDetails()
        setClickListners()
        observeChanges()
    }

    fun observeChanges(){
        viewModel.surveyProperties.observe(this, Observer {resource->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    setSpinnerData(resource.data)
                   context?.shortToast("data coming")
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    if (resource.error != AppError.WaitingForNetwork) {
                        handleError(resource.error)
                    }
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }

        })
    }


    fun setSpinnerData(data:GetSurveyProperties?){

        spGender?.setArrayAdapter(data?.gender?.toList())
        spRace?.setArrayAdapter(data?.race?.toList())
        spHouseHold?.setArrayAdapter(data?.houseHoldIncome?.toList())
        spHomeOwnership?.setArrayAdapter(data?.homeOwnership?.toList())
        spEducation?.setArrayAdapter(data?.education?.toList())
        spEmployementStatus?.setArrayAdapter(data?.employementStatus?.toList())
        spMaritalStatus?.setArrayAdapter(data?.maritalStatus?.toList())


    }




//    fun initChilds(){
//
//
//            val view = LayoutInflater.from(context)
//
//            val child = view.inflate(R.layout.custom_spinner_layout, null)
//            val params = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//
//        child.tvName.text = "male"
//
//        child.spinner.setArrayAdapter(list)
//
//        llSpinners?.addView(child,params)
//
//
//    }

    fun setClickListners(){
        tvQuestions.setOnClickListener {

            view?.findNavController()?.navigate(R.id.surveyFragment)
        }

    }

}