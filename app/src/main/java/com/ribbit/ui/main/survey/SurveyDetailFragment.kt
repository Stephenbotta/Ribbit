package com.ribbit.ui.main.survey

import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.ribbit.R
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.survey.*
import com.ribbit.extensions.*
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
        const val SURVEY_TIME = "SURVEY_TIME"
        fun newInstance(fromTab: Boolean): SurveyDetailFragment {
            val profileFragment = SurveyDetailFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    var surveyID:String = ""
    var surveyTIME:Int = 10
    var gloabalList = mutableListOf<Questions>()
    var quizIndex = 0

    var globalRequest = RequestSubmitSurvey()

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_detail

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyID = arguments?.getString(SURVEY_ID) ?: ""
        surveyTIME = arguments?.getInt(SURVEY_TIME) ?: 10
        loadingDialog = LoadingDialog(context!!)


        GlideApp.with(requireContext()).load(com.ribbit.data.local.UserManager.getProfile().image?.thumbnail).into(imageView2)
        textView9.text = "Ends in ${surveyTIME}mins"

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
        tvQuizNo.text = "Question No. ${quizIndex+1}"
        btnGetStarted3.visible()
        when (gloabalList[quizIndex].mediaType) {
            "TEXT" -> {
                imageView3.gone()
            }
            "IMAGE" -> {
                imageView3.visible()
                GlideApp.with(requireContext()).load(gloabalList[quizIndex].imageUrl).into(imageView3)
            }
            else -> {
                imageView3.visible()
                GlideApp.with(requireContext()).load(gloabalList[quizIndex].imageUrl).into(imageView3)
                imageView3.setOnClickListener {
                    VideoPlayerActivity.start(context!!,  gloabalList[quizIndex].imageUrl ?: "")
                }
            }
        }

        tvQuestions.text = gloabalList[quizIndex].question
        initOptions(gloabalList[quizIndex].optionList)
    }


    fun  fillDemoList(data: GetSurveyList?) {

        data?.info?.forEach {
            val media =it.media  as? LinkedTreeMap<String,String>
            val mediaType = media?.getValue("mediaType")
            val thumbnail = media?.getValue("thumbnail")

        //    Log.d("meddd","" + thumbnail)
            gloabalList.add(Questions(imageUrl = thumbnail,mediaType = mediaType,quesId = it._id,question = it.name,optionList=it.options))
        }
    }

    fun setClickListners(){



        imageViewClose.setOnClickListener { showLogoutConfirmationDialog() }

        btnGetStarted3.setOnClickListener {

            if (!checkValidations())
                return@setOnClickListener

            if (gloabalList.size == 1){
                makeSurveySubmitRequest()
                return@setOnClickListener
            }

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
            cb.setTextColor(resources.getColor(R.color.white))
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


    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(context!!, R.style.AppDialog)
                .setMessage(R.string.survey_message_confirm_exit)
                .setPositiveButton(R.string.survey_btn_exit) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(context!!, R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context!!, R.color.white))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(ContextCompat.getColor(context!!, R.color.transparent))

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context!!, R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(ContextCompat.getColor(context!!, R.color.transparent))
    }


}