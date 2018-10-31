package com.conversify.ui.loginsignup.verification

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.clickSpannable
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.createpassword.CreatePasswordFragment
import kotlinx.android.synthetic.main.fragment_verification.*

class VerificationFragment : BaseFragment() {
    companion object {
        const val TAG = "VerificationFragment"
        private const val ARGUMENT_REGISTERED_MODE = "ARGUMENT_REGISTERED_MODE"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"

        fun newInstance(registeredMode: Int, profile: ProfileDto): Fragment {
            val fragment = VerificationFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_REGISTERED_MODE, registeredMode)
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_verification

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
    }

    private fun setListeners() {
        fabProceed.setOnClickListener {
            val fragment = CreatePasswordFragment()
            fragmentManager?.apply {
                beginTransaction()
                        .add(R.id.flContainer, fragment, CreatePasswordFragment.TAG)
                        .addToBackStack(null)
                        .commit()
            }
        }

        val spannableText = getString(R.string.verification_label_resend_code)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.brandon_text_bold)
        tvLabelDidNotReceiveCode.clickSpannable(spannableText, R.color.colorPrimary, typeface,
                View.OnClickListener {})
    }
}