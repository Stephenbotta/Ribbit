package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.survey.GetSurveyList
import com.ribbit.data.remote.models.survey.Options
import com.ribbit.data.remote.models.survey.OptionsList
import com.ribbit.data.remote.models.survey.Questions
import com.ribbit.extensions.handleError
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.videoplayer.VideoPlayerActivity
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
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                     fillDemoList(resource.data)
                     tvQuestions.text = gloabalList[quizIndex].question
                     initOptions(gloabalList[quizIndex].optionList)
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



    fun  fillDemoList(data: GetSurveyList?) {


        data?.info?.forEach {

            gloabalList.add(Questions(it.name,optionList=it.options))

        }

//        var optionList = mutableListOf<OptionsList>()
//        optionList.add(OptionsList("name"))
//        optionList.add(OptionsList("age"))
//        optionList.add(OptionsList("number"))
//
//
//
//
//        gloabalList.add(Questions(" fdfs 2",optionList=optionList2))
//        gloabalList.add(Questions("jidfgg 3",optionList=optionList3))
//        gloabalList.add(Questions("jidfgg df 4",optionList=optionList4))
    }

    fun setClickListners(){

        imageView3.setOnClickListener {
            VideoPlayerActivity.start(context!!,  "https://youtu.be/SlPhMPnQ58k")
        }

        imageViewClose.setOnClickListener { findNavController().navigateUp() }

        btnGetStarted3.setOnClickListener {

            if (!checkValidations())
                return@setOnClickListener

            if (quizIndex == gloabalList.size-1){
                context?.shortToast("Quiz finished thanks")
                findNavController().navigateUp()
                return@setOnClickListener
            }

            if (quizIndex<gloabalList.size)
            {
                quizIndex ++
                tvQuestions.text = gloabalList[quizIndex].question
                initOptions(gloabalList[quizIndex].optionList)

                if (quizIndex == gloabalList.size-1){
                    btnGetStarted3.text = "Finished"
                }
            }

        }

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