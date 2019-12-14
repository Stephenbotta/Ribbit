package com.ribbit.ui.main.survey

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.survey.*
import com.ribbit.extensions.handleError
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.videoplayer.VideoPlayerActivity
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_survey_detail.*


class SurveyDetailFragment : BaseFragment() {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"
        const val SURVEY_ID = "SURVEY_ID"

        fun newInstance(fromTab: Boolean): SurveyDetailFragment {
            val profileFragment = SurveyDetailFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    var surveyID:String = ""
    var gloabalList = mutableListOf<Questions>()
    var quizIndex = 0

    var globalRequest = RequestSubmitSurvey()

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_detail

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyID = arguments?.getString(SURVEY_ID) ?: ""
        loadingDialog = LoadingDialog(context!!)

        viewModel.getQuestionList(surveyID)
        observeChanges()
        setClickListners()
    }


    fun observeChanges(){
        viewModel.surveyList.observe(this, Observer {resource->
            resource.data?.info ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    if (resource.data.info.isNullOrEmpty())
                    {
                        context?.shortToast("Empty questions list")
                        return@Observer
                    }

                     fillDemoList(resource.data)
                    updateUI()
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

    fun updateUI(){
        GlideApp.with(requireContext()).load(gloabalList[quizIndex].imageUrl).into(imageView3)
        tvQuestions.text = gloabalList[quizIndex].question
        initOptions(gloabalList[quizIndex].optionList)
    }


    fun  fillDemoList(data: GetSurveyList?) {

        data?.info?.forEach {
            val media = it.media as?  ImageUrlDto
            gloabalList.add(Questions(imageUrl = media?.original,quesId = it._id,question = it.name,optionList=it.options))
        }
    }

    fun setClickListners(){

        imageView3.setOnClickListener {
            VideoPlayerActivity.start(context!!,  "https://youtu.be/SlPhMPnQ58k")
        }

        imageViewClose.setOnClickListener { findNavController().navigateUp() }

        btnGetStarted3.setOnClickListener {

            if (!checkValidations())
                return@setOnClickListener

            // when user is at last quiz..
            if (quizIndex == gloabalList.size-1){
                // add selected data to request here..
                updateRequestData()
                makeSurveySubmitRequest()
                return@setOnClickListener
            }

            if (quizIndex<gloabalList.size)
            {
                quizIndex ++
                updateUI()

                // add selected data to request here..
                updateRequestData()

                if (quizIndex == gloabalList.size-1){
                    btnGetStarted3.text = "Finished"
                }
            }
        }
    }


    fun updateRequestData(){
        // add selected data to request here..
        val quesObj = Ques()
        quesObj.questionId = gloabalList[quizIndex-1].quesId

        val selectedOption = gloabalList[quizIndex-1].optionList?.filter { it.isSelected }
        quesObj.options?.add(OptionsList().apply { optionId = selectedOption?.get(0)?._id})
        globalRequest.questions?.add(quesObj)

        Log.d("quesS",Gson().toJson(globalRequest.questions))
    }


    fun makeSurveySubmitRequest(){
        viewModel.submitSurveyQuiz(surveyID,Gson().toJson(globalRequest.questions))

        viewModel.submitSurvey.observe(this, Observer {resource ->

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    context?.shortToast("Survey Submitted Successfully")
                    findNavController().navigateUp()

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


    fun initOptions(list: List<Options>?){

        linLayout.removeAllViews()
        list?.forEach {

            var cb = CheckBox(context)
            cb.text = it.name
            linLayout.addView(cb)

            // clickListner
            cb.setOnCheckedChangeListener { compoundButton, b ->

                // get current check-box index here...
                var index = linLayout.indexOfChild(cb)

                // if user presses checkboxes to true
                if(b){
                    val childCount: Int = linLayout.getChildCount()

                    // remove all previous checkBoxes and listValue
                    for (i in 0 until childCount) {
                        val v: CheckBox = linLayout.getChildAt(i) as CheckBox
                        v.isChecked = false
                        gloabalList[quizIndex].optionList?.forEach {
                            it.isSelected = false
                        }
                    }

                    // enable current check and listValue
                    val v: CheckBox = linLayout.getChildAt(index) as CheckBox
                    v.isChecked = true
                    gloabalList[quizIndex]?.optionList?.get(index)?.isSelected = true
                }

            }
        }


    }


    fun checkValidations():Boolean{

        gloabalList[quizIndex].optionList?.forEach {
            if (it.isSelected)
                return true
        }
        context?.shortToast("Please select an option")
        return false
    }

}