package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.survey.GetSurveyList
import com.ribbit.data.remote.models.survey.RequestSubmitSurvey
import com.ribbit.data.remote.models.survey.SurveyList
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_survey_detail.*

class SurveyDetailFragment : BaseFragment() {
    companion object {
        private const val CHILD_LOADING = 0
        private const val CHILD_ERROR = 1
        private const val CHILD_SURVEY = 2

        const val TAG = "SurveyFragment"
        const val SURVEY = "SURVEY"
    }

    private val survey by lazy { arguments?.getParcelable(SURVEY) ?: SurveyList() }
    private val profile by lazy { UserManager.getProfile() }

    var globalRequest = RequestSubmitSurvey()

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_detail

    private val viewModel by lazy { ViewModelProviders.of(this)[SurveyViewModel::class.java] }
    private lateinit var loadingDialog: LoadingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())


        GlideApp.with(requireContext()).load(profile.image?.thumbnail).into(ivProfile)
        tvSurveyTime.text =
                view.context.getString(R.string.ends_in_survey_time, survey.totalTime ?: 0)
        tvSurveyTitle.text = survey.name ?: ""

        if (requireContext().isNetworkActiveWithMessage())
            viewModel.getQuestionList(survey._id ?: "")
        else
            viewFlipper.displayedChild = CHILD_ERROR

        observeChanges()
        setClickListners()
    }


    fun observeChanges() {
        viewModel.surveyList.observe(this, Observer { resource ->
            resource.data?.info ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    viewFlipper.displayedChild = CHILD_SURVEY
                    val questions = resource.data.info
                    if (questions.isNullOrEmpty()) {
                        context?.shortToast(getString(R.string.empty_question_list))
                        return@Observer
                    }

                    fillDemoList(resource.data)
                    updateUI()
                }

                Status.ERROR -> {
                    viewFlipper.displayedChild = CHILD_ERROR
                    if (resource.error != AppError.WaitingForNetwork) {
                        handleError(resource.error)
                    }
                }

                Status.LOADING -> {
                    viewFlipper.displayedChild = CHILD_LOADING
                }
            }

        })
    }

    fun updateUI() {
        /*tvQuizNo.text = "Question No. ${quizIndex + 1}"
        tvDone.visible()
        when (gloabalList[quizIndex].mediaType) {
            "TEXT" -> {
                ivQuestion.gone()
            }
            "IMAGE" -> {
                ivQuestion.visible()
                GlideApp.with(requireContext()).load(gloabalList[quizIndex].imageUrl).into(ivQuestion)
            }
            else -> {
                ivQuestion.visible()
                GlideApp.with(requireContext()).load(gloabalList[quizIndex].imageUrl).into(ivQuestion)
                ivQuestion.setOnClickListener {
                    VideoPlayerActivity.start(requireContext(), gloabalList[quizIndex].imageUrl
                            ?: "")
                }
            }
        }

        tvQuestion.text = gloabalList[quizIndex].question
        initOptions(gloabalList[quizIndex].optionList)*/
    }


    fun fillDemoList(data: GetSurveyList?) {

        /*  data?.info?.forEach {
              val media = it.media as? LinkedTreeMap<String, String>
              val mediaType = media?.getValue("mediaType")
              val thumbnail = media?.getValue("thumbnail")

              //    Log.d("meddd","" + thumbnail)
              gloabalList.add(Questions(imageUrl = thumbnail, mediaType = mediaType, quesId = it._id, question = it.name, optionList = it.options))
          }*/
    }

    fun setClickListners() {


        imageViewClose.setOnClickListener { showLogoutConfirmationDialog() }

        /*tvDone.setOnClickListener {

            if (!checkValidations())
                return@setOnClickListener

            if (gloabalList.size == 1) {
                makeSurveySubmitRequest()
                return@setOnClickListener
            }

            // when user is at last quiz..
            if (quizIndex == gloabalList.size - 1) {
                // add selected data to request here..
                updateRequestData()
                makeSurveySubmitRequest()
                return@setOnClickListener
            }

            if (quizIndex < gloabalList.size) {
                quizIndex++
                updateUI()

                // add selected data to request here..
                updateRequestData()

                if (quizIndex == gloabalList.size - 1) {
                    tvDone.text = "Finished"
                }
            }
        }*/
    }


    /*fun updateRequestData() {
        // add selected data to request here..
        val quesObj = Ques()
        quesObj.questionId = gloabalList[quizIndex - 1].quesId

        val selectedOption = gloabalList[quizIndex - 1].optionList.filter { it.isSelected }
        quesObj.options.add(OptionsList().apply { optionId = selectedOption.get(0)._id })
        globalRequest.questions.add(quesObj)

        Log.d("quesS", Gson().toJson(globalRequest.questions))
    }*/


    fun makeSurveySubmitRequest() {
        viewModel.submitSurveyQuiz(survey._id ?: "", Gson().toJson(globalRequest.questions))

        viewModel.submitSurvey.observe(this, Observer { resource ->

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


    /*fun initOptions(list: List<Options>?) {

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
                if (b) {
                    val childCount: Int = linLayout.childCount

                    // remove all previous checkBoxes and listValue
                    for (i in 0 until childCount) {
                        val v: CheckBox = linLayout.getChildAt(i) as CheckBox
                        v.isChecked = false
                        gloabalList[quizIndex].optionList.forEach {
                            it.isSelected = false
                        }
                    }

                    // enable current check and listValue
                    val v: CheckBox = linLayout.getChildAt(index) as CheckBox
                    v.isChecked = true
                    gloabalList[quizIndex].optionList.get(index).isSelected = true
                }

            }
        }


    }*/


    /*  fun checkValidations(): Boolean {

          gloabalList[quizIndex].optionList.forEach {
              if (it.isSelected)
                  return true
          }
          context?.shortToast("Please select an option")
          return false
      }*/


    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.AppDialog)
                .setMessage(R.string.survey_message_confirm_exit)
                .setPositiveButton(R.string.survey_btn_exit) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
    }
}