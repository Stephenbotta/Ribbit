package com.conversify.ui.loginsignup.verification

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.createpassword.CreatePasswordFragment
import com.conversify.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_verification.*

class VerificationFragment : BaseFragment() {
    companion object {
        const val TAG = "VerificationFragment"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"

        fun newInstance(profile: ProfileDto): Fragment {
            val fragment = VerificationFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: VerificationViewModel
    private lateinit var loadingDialog: LoadingDialog

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_verification

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VerificationViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        val profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        viewModel.start(profile)

        displayVerificationDetails(profile)
        setListeners()
        observeChanges()
    }

    private fun displayVerificationDetails(profile: ProfileDto) {
        if (viewModel.isRegisteredModePhone()) {
            tvLabelWeSentYou.setText(R.string.verification_label_we_sent_you_a_code_phone)

            val fullPhoneNumber = String.format("%s %s", profile.countryCode, profile.phoneNumber)
            tvLabelCodeSentTo.text = getString(R.string.verification_label_code_sent_to, fullPhoneNumber)
        } else {
            tvLabelWeSentYou.setText(R.string.verification_label_we_sent_you_a_code_email)
            tvLabelCodeSentTo.text = getString(R.string.verification_label_code_sent_to, profile.email)
        }
    }

    private fun setListeners() {
        fabProceed.setOnClickListener {
            val otp = etEnterCode.text.toString()
            when {
                otp.isEmpty() -> {
                    requireActivity().shortToast(R.string.error_empty_otp)
                }

                !ValidationUtils.isOtpValid(otp) -> {
                    requireActivity().shortToast(R.string.error_invalid_otp)
                }

                requireActivity().isNetworkActiveWithMessage() -> {
                    viewModel.verifyOtp(otp)
                }
            }
        }

        val spannableText = getString(R.string.verification_label_resend_code)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.brandon_text_bold)
        tvLabelDidNotReceiveCode.clickSpannable(spannableText, R.color.colorPrimary, typeface,
                View.OnClickListener {
                    if (requireActivity().isNetworkActiveWithMessage()) {
                        viewModel.resendOtp()
                    }
                })
    }

    private fun observeChanges() {
        viewModel.verifyOtp.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val profile = resource.data
                    if (profile != null) {
                        val fragment = CreatePasswordFragment.newInstance(profile)
                        fragmentManager?.apply {
                            beginTransaction()
                                    .add(R.id.flContainer, fragment, CreatePasswordFragment.TAG)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })

        viewModel.resendOtp.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    requireActivity().longToast(R.string.verification_message_otp_resent_successfully)
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    requireActivity().handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}