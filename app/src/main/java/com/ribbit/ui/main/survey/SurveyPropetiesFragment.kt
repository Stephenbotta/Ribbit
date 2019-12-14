package com.ribbit.ui.main.survey

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.survey.GetSurveyProperties
import com.ribbit.data.remote.models.survey.RequestSurveyProperties
import com.ribbit.extensions.handleError
import com.ribbit.extensions.setArrayAdapter
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import kotlinx.android.synthetic.main.fragment_survey_data.*


class SurveyPropetiesFragment : BaseFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): SurveyPropetiesFragment {
            val profileFragment = SurveyPropetiesFragment()
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


    var model = RequestSurveyProperties()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        loadingDialog = LoadingDialog(context!!)
        //  spGender?.setArrayAdapter(list)

     //   initChilds()
        viewModel.getSurveyProperties()
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
                 //  context?.shortToast("data coming")
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


        spDOB.setOnClickListener {
            val dialog = DatePickerDialog(context, this, 2013, 2, 18)
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }



        spinnerListners()
    }


    fun spinnerListners(){
        spGender.onItemSelectedListener = itemLisner
        spRace.onItemSelectedListener = itemLisner
        spHouseHold.onItemSelectedListener = itemLisner
        spHomeOwnership.onItemSelectedListener = itemLisner
        spEducation.onItemSelectedListener = itemLisner
        spEmployementStatus.onItemSelectedListener = itemLisner
        spMaritalStatus.onItemSelectedListener = itemLisner
    }


    val itemLisner = object : OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {

            val selectedValue = adapterView.getItemAtPosition(i).toString()

            when(adapterView.id){

                R.id.spGender ->{
                    model.gender = selectedValue
                }
                R.id.spRace ->{
                    model.race = selectedValue
                }
                R.id.spHouseHold ->{
                    model.houseHoldIncome = selectedValue
                }
                R.id.spHomeOwnership ->{
                    model.homeOwnership = selectedValue
                }
                R.id.spEducation ->{
                    model.education = selectedValue
                }
                R.id.spEmployementStatus ->{
                    model.employementStatus = selectedValue
                }
                R.id.spMaritalStatus ->{
                    model.maritalStatus = selectedValue
                }
            }

            try { //Your task here
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
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


    fun checkValidations():Boolean{

        if (spDOB.text == "Select date of birth" ){
            context?.shortToast("Please select date of birth")
            return false
        }

         if (!cbTerms.isChecked){
             context?.shortToast("Please agree to the terms and conditions")
             return false
         }

        return true
    }

    fun setClickListners(){
        tvQuestions.setOnClickListener {
           //

            if (!checkValidations())
                return@setOnClickListener

            viewModel.takeSurveyProperties(model)

            viewModel.takeSurveyProperties.observe(this, Observer {resource ->

                when (resource.status) {
                    Status.SUCCESS -> {
                        loadingDialog.setLoading(false)
                        view?.findNavController()?.navigate(R.id.surveyFragment)
                        val profile = UserManager.getProfile()
                        profile.isTakeSurvey = true
                        UserManager.saveProfile(profile)
                        //  context?.shortToast("data coming")
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

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        spDOB.text = "$p3/$p2/$p1"
    }

}