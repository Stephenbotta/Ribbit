package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.extensions.gone
import com.ribbit.extensions.launchActivity
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseFragment
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_user_statics.*

class SurveyUserStaticsFragment : BaseFragment() {
    companion object {
        const val TAG = "SurveyUserStaticsFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_user_statics
    private val profile by lazy { UserManager.getProfile() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(this)
                .load(profile.image?.original)
                .into(ivProfile)
        tvName.text = profile.fullName
        if (profile.bio.isNullOrBlank()) {
            tvBio.gone()
        } else {
            tvBio.visible()
            tvBio.text = profile.bio
        }

        //todo these keys not coming in login api and update keys after their steps completion
        tvSurveyCount.text = String.format("%02d", profile.totalSurveys ?: 0)
        tvPointsCount.text = String.format("%02d", profile.pointEarned ?: 0)
        tvRedeemedCount.text = String.format("%02d", profile.pointRedeemed ?: 0)
        setClickListners()
    }

    private fun setClickListners() {
        tvQuestion.setOnClickListener {
            context?.launchActivity<SurveyActivity> { }
        }
    }
}