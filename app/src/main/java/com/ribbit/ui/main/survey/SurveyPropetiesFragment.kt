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
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import kotlinx.android.synthetic.main.fragment_survey_data.*
import java.util.*


class SurveyPropetiesFragment : BaseFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        const val TAG = "SurveyPropetiesFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_data

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    var model = RequestSurveyProperties()
    private lateinit var loadingDialog: LoadingDialog
    private val calendar by lazy { Calendar.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(view.context)
        if (requireContext().isNetworkActiveWithMessage())
            viewModel.getSurveyProperties()
        setClickListeners()
        observeChanges()
    }

    fun observeChanges() {
        viewModel.surveyProperties.observe(this, Observer { resource ->
            resource ?: return@Observer
            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    setSpinnerData(resource.data)
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

        viewModel.takeSurveyProperties.observe(this, Observer { resource ->

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    view?.findNavController()?.navigate(R.id.action_surveyDataFragment_to_surveyFragment)
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

    private fun setSpinnerData(data: GetSurveyProperties?) {
        spGender?.setArrayAdapter(data?.gender?.map { it })
        spRace?.setArrayAdapter(data?.race?.map { it })
        spHouseHold?.setArrayAdapter(data?.houseHoldIncome?.map { it })
        spHomeOwnership?.setArrayAdapter(data?.homeOwnership?.map { it })
        spEducation?.setArrayAdapter(data?.education?.map { it })
        spEmployementStatus?.setArrayAdapter(data?.employementStatus?.map { it })
        spMaritalStatus?.setArrayAdapter(data?.maritalStatus?.map { it })

        spDOB.setOnClickListener {
            val dialog = DatePickerDialog(requireContext(), this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }

        spinnerListeners()

        spDOB.text = parseDob(data?.dateOfBirth)
        model.dateOfBirth = data?.dateOfBirth
    }

    private fun spinnerListeners() {
        spGender.onItemSelectedListener = itemListener
        spRace.onItemSelectedListener = itemListener
        spHouseHold.onItemSelectedListener = itemListener
        spHomeOwnership.onItemSelectedListener = itemListener
        spEducation.onItemSelectedListener = itemListener
        spEmployementStatus.onItemSelectedListener = itemListener
        spMaritalStatus.onItemSelectedListener = itemListener
    }


    private val itemListener = object : OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
            val selectedValue = adapterView.getItemAtPosition(i).toString()
            when (adapterView.id) {
                R.id.spGender -> {
                    model.gender = selectedValue
                }
                R.id.spRace -> {
                    model.race = selectedValue
                }
                R.id.spHouseHold -> {
                    model.houseHoldIncome = selectedValue
                }
                R.id.spHomeOwnership -> {
                    model.homeOwnership = selectedValue
                }
                R.id.spEducation -> {
                    model.education = selectedValue
                }
                R.id.spEmployementStatus -> {
                    model.employementStatus = selectedValue
                }
                R.id.spMaritalStatus -> {
                    model.maritalStatus = selectedValue
                }
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }


    private fun checkValidations(): Boolean {
        if (spDOB.text == getString(R.string.select_date_of_birth)) {
            context?.shortToast(getString(R.string.please_select_date_of_birth))
            return false
        }

        if (!cbTerms.isChecked) {
            context?.shortToast(getString(R.string.please_agree_to_terms_and_conditions))
            return false
        }

        return true
    }

    private fun setClickListeners() {
        tvQuestion.setOnClickListener {
            if (!checkValidations())
                return@setOnClickListener

            if (requireContext().isNetworkActiveWithMessage()) {
                viewModel.takeSurveyProperties(model)
            }
        }
    }

    override fun onDateSet(picker: DatePicker, year: Int, month: Int, day: Int) {
        spDOB.text = String.format("%02d/%02d/%d", day, month + 1, year)

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, month)

        model.dateOfBirth = calendar.timeInMillis
    }
}