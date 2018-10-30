package com.conversify.ui.loginsignup.welcome

import android.content.Context
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.BackButtonEnabledListener
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment() {
    companion object {
        const val TAG = "WelcomeFragment"
    }

    private var backButtonEnabledListener: BackButtonEnabledListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BackButtonEnabledListener) {
            backButtonEnabledListener = context
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_welcome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButtonEnabledListener?.onBackButtonEnabled(false)

        fabProceed.setOnClickListener { }
    }
}