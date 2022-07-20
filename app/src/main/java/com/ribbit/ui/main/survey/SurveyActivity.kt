package com.ribbit.ui.main.survey

import android.os.Bundle
import androidx.navigation.findNavController
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.ui.base.BaseActivity

class SurveyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        val profile = UserManager.getProfile()
        if (profile.isTakeSurvey == true) {
            findNavController(R.id.my_nav_host_fragment).navigate(R.id.action_surveyDataFragment_to_surveyFragment)
        }
    }
}
