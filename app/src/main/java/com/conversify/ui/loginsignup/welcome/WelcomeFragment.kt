package com.conversify.ui.loginsignup.welcome

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.gone
import com.conversify.extensions.handleError
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.BackButtonEnabledListener
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment() {
    companion object {
        private const val ARGUMENT_PROFILE = "ARGUMENT_PROFILE"
        private const val ARGUMENT_PASSWORD = "ARGUMENT_PASSWORD"
        const val TAG = "WelcomeFragment"

        fun newInstance(profile: ProfileDto, password: String? = null): Fragment {
            val fragment = WelcomeFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_PROFILE, profile)
            if (password != null) {
                arguments.putString(ARGUMENT_PASSWORD, password)
            }
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: WelcomeViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var profile: ProfileDto
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

        profile = arguments?.getParcelable(ARGUMENT_PROFILE) as ProfileDto
        backButtonEnabledListener?.onBackButtonEnabled(false)

        viewModel = ViewModelProviders.of(this)[WelcomeViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())

        setupViews()
        setListeners()
        observeChanges()
    }

    private fun setupViews() {
        // Hide email fields if email exists in profile
        if (!profile.email.isNullOrBlank()) {
            tvLabelEmail.gone()
            etEmail.gone()
        }

        // Hide phone number fields if phone number exists in profile
        if (!profile.phoneNumber.isNullOrBlank()) {
            tvLabelPhoneNumber.gone()
            countryCodePicker.gone()
            etPhoneNumber.gone()
        }
    }

    private fun setListeners() {
        fabProceed.setOnClickListener {
            val fullName = etFullName.text.toString()
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()

            val fragment = ChooseInterestsFragment()
            fragmentManager?.apply {
                beginTransaction()
                        .add(R.id.flContainer, fragment, ChooseInterestsFragment.TAG)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }

    private fun observeChanges() {
        viewModel.signUp.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

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