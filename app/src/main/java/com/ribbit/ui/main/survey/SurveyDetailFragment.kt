package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.ribbit.R
import com.ribbit.data.remote.models.survey.OptionsList
import com.ribbit.data.remote.models.survey.Questions
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_survey_detail.*


class SurveyDetailFragment : BaseFragment() {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): SurveyDetailFragment {
            val profileFragment = SurveyDetailFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    var gloabalList = mutableListOf<Questions>()
    var quizIndex = 0

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_survey_detail

    private val viewModel by lazy { ViewModelProviders.of(this)[ProfileViewModel::class.java] }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fillDemoList()
        setClickListners()
    }


    fun  fillDemoList(){
        var optionList = mutableListOf<OptionsList>()
        optionList.add(OptionsList("name"))
        optionList.add(OptionsList("age"))
        optionList.add(OptionsList("number"))

        var optionList2 = mutableListOf<OptionsList>()
        optionList2.add(OptionsList("bahu"))
        optionList2.add(OptionsList("munna"))
        optionList2.add(OptionsList("tej"))

        var optionList3 = mutableListOf<OptionsList>()
        optionList3.add(OptionsList("men"))
        optionList3.add(OptionsList("wo-men"))
        optionList3.add(OptionsList("age"))

        var optionList4 = mutableListOf<OptionsList>()
        optionList4.add(OptionsList("india"))
        optionList4.add(OptionsList("south"))
        optionList4.add(OptionsList("bhutan"))



        gloabalList.add(Questions("jidfgg",optionList=optionList))
        gloabalList.add(Questions(" fdfs 2",optionList=optionList2))
        gloabalList.add(Questions("jidfgg 3",optionList=optionList3))
        gloabalList.add(Questions("jidfgg df 4",optionList=optionList4))
    }

    fun setClickListners(){

        btnGetStarted3.setOnClickListener {



            if (quizIndex > gloabalList.size){
                context?.shortToast("Quiz finished thanks")
                findNavController().navigateUp()
                return@setOnClickListener
            }

            if (quizIndex>0 && !checkValidations())
                return@setOnClickListener

            if (quizIndex<gloabalList.size)
            {
                tvQuestions.text = gloabalList[quizIndex].question
                initOptions(gloabalList[quizIndex].optionList)
                quizIndex ++
            }
            else{
                btnGetStarted3.text = "Finished"
                quizIndex ++
            }

        }
        btnGetStarted3.performClick()
    }


    fun initOptions(list: List<OptionsList>){

        linLayout.removeAllViews()
        list.forEach {

            var cb = CheckBox(context)
            cb.text = it.option
            linLayout.addView(cb)

            // clickListner
            cb.setOnCheckedChangeListener { compoundButton, b ->

                var index = linLayout.indexOfChild(cb)

                // if user presses checkboxes to true
                if(b){
                    val childCount: Int = linLayout.getChildCount()

                    // remove all previous checkBoxes and listValue
                    for (i in 0 until childCount) {
                        val v: CheckBox = linLayout.getChildAt(i) as CheckBox
                        v.isChecked = false
                        gloabalList[quizIndex-1].optionList.forEach {
                            it.isSelected = false
                        }
                    }

                    // enable current check and listValue
                    val v: CheckBox = linLayout.getChildAt(index) as CheckBox
                    v.isChecked = true
                    gloabalList[quizIndex-1].optionList[index].isSelected = true
                }

            }
        }


    }


    fun checkValidations():Boolean{

        gloabalList[quizIndex-1].optionList.forEach {
            if (it.isSelected)
                return true
        }
        context?.shortToast("Please select an option")
        return false
    }


}