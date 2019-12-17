package com.ribbit.ui.main.survey

import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.extensions.shortToast
import com.ribbit.ui.base.BaseActivity

class SurveyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        val profile = UserManager.getProfile()
        if (profile.isTakeSurvey == true){
            findNavController(R.id.my_nav_host_fragment).navigate(R.id.action_surveyDataFragment_to_surveyFragment)
        }


//        val navController = findNavController(R.id.my_nav_host_fragment)
//
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.surveyDataFragment -> {
//                    val profile = UserManager.getProfile()
//                         if (profile.isTakeSurvey == true){
//                             findNavController(R.id.my_nav_host_fragment).navigate(R.id.action_surveyDataFragment_to_surveyFragment)
//                        }
//                    }
//
//                }
//
//
//            }
//        }

    }
}
