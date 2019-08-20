package com.checkIt.ui.loginsignup.verification

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.checkIt.R
import com.checkIt.data.local.PrefsManager
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.extensions.*
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.custom.LoadingDialog
import com.checkIt.ui.loginsignup.createpassword.CreatePasswordFragment
import com.checkIt.utils.AppConstants
import com.checkIt.utils.ValidationUtils
import kotlinx.android.synthetic.main.fragment_verification.*

class VerificationFragment : BaseFragment() {
    companion object {
        const val TAG = "VerificationFragment"
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        private const val ARGUMENT_STARTED_FOR_RESULT = "ARGUMENT_STARTED_FOR_RESULT"

        fun newInstance(profile: ProfileDto, startedForResult: Boolean = false): Fragment {
            val fragment = VerificationFragment()
            val arguments = Bundle()
            arguments.putBoolean(ARGUMENT_STARTED_FOR_RESULT, startedForResult)
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            fragment.arguments = arguments
            return fragment
        }
    }


    private val startedForResult: Boolean by lazy {
        arguments?.getBoolean(VerificationFragment.ARGUMENT_STARTED_FOR_RESULT) ?: false
    }
    private lateinit var viewModel: VerificationViewModel
    private lateinit var loadingDialog: LoadingDialog

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_verification

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VerificationViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        val profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        viewModel.start(profile, startedForResult)

        fabProceed.isEnabled = false

        displayVerificationDetails(profile)
        setListeners()
        observeChanges()
    }

    private fun displayVerificationDetails(profile: ProfileDto) {
        if (startedForResult) {
            tvLabelWeSentYou.setText(R.string.verification_label_we_sent_you_a_code_phone)

            val fullPhoneNumber = String.format("%s %s", profile.countryCode, profile.phoneNumber)
            tvLabelCodeSentTo.text = getString(R.string.verification_label_code_sent_to, fullPhoneNumber)
        } else {
            if (viewModel.isRegisteredModePhone()) {
                tvLabelWeSentYou.setText(R.string.verification_label_we_sent_you_a_code_phone)
                val fullPhoneNumber = if (profile.countryCode == null) {
                    String.format("%s %s", PrefsManager.get().getString("countryCode", ""), profile.phoneNumber)
                } else {
                    String.format("%s %s", profile.countryCode, profile.phoneNumber)
                }
//                val fullPhoneNumber = String.format("%s %s", profile.countryCode, profile.phoneNumber)
                tvLabelCodeSentTo.text = getString(R.string.verification_label_code_sent_to, fullPhoneNumber)
            } else {
                tvLabelWeSentYou.setText(R.string.verification_label_we_sent_you_a_code_email)
                tvLabelCodeSentTo.text = getString(R.string.verification_label_code_sent_to, profile.email)
            }
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

        etEnterCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val otp = text?.toString() ?: ""
                fabProceed.isEnabled = ValidationUtils.isOtpValid(otp)
            }
        })

        val spannableText = getString(R.string.verification_label_resend_code)
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.roboto_text_bold)
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
                        if (startedForResult) {
                            val intent = Intent()
                            targetFragment?.onActivityResult(AppConstants.REQ_CODE_VERIFICATION, Activity.RESULT_OK, intent)
                            requireActivity().onBackPressed()
                        } else {
                            val fragment = CreatePasswordFragment.newInstance(profile)
                            fragmentManager?.apply {
                                beginTransaction()
                                        .setCustomAnimations(R.anim.parallax_right_in, R.anim.parallax_left_out,
                                                R.anim.parallax_left_in, R.anim.parallax_right_out)
                                        .add(R.id.flContainer, fragment, CreatePasswordFragment.TAG)
                                        .addToBackStack(null)
                                        .commit()
                            }
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