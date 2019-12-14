package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_survey_data.*

class SurveyUserStatics : BaseFragment() {
    companion object {
        const val TAG = "SurveyFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): SurveyUserStatics {
            val profileFragment = SurveyUserStatics()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    private val fromTab by lazy { arguments?.getBoolean(ARGUMENT_FROM_TAB) ?: true }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_user_statics

    private val viewModel by lazy { ViewModelProviders.of(this)[ProfileViewModel::class.java] }
    private lateinit var interestsAdapter: SurveyAdapter


    val list = mutableListOf<String>("Male","Gender","Female")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListners()

    }




    fun setClickListners(){

        tvQuestions.setOnClickListener {
            context?.launchActivity<SurveyActivity> { }
        }
    }

}