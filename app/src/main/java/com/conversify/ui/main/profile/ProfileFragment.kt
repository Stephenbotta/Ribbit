package com.conversify.ui.main.profile

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment(), ProfileInterestsAdapter.Callback {
    companion object {
        const val TAG = "ProfileFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_profile

    private val viewModel by lazy { ViewModelProviders.of(this)[ProfileViewModel::class.java] }
    private lateinit var interestsAdapter: ProfileInterestsAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInterestsRecycler()
        displayProfile(viewModel.getProfile())
        btnLogout.setOnClickListener {
            if (isNetworkActiveWithMessage()) {
                viewModel.logout()
            }
        }
        observeChanges()
    }

    private fun setupInterestsRecycler() {
        interestsAdapter = ProfileInterestsAdapter(this)
        val layoutManager = FlexboxLayoutManager(requireActivity())
        layoutManager.flexWrap = FlexWrap.WRAP
        rvInterests.layoutManager = layoutManager
        rvInterests.isNestedScrollingEnabled = false
        rvInterests.adapter = interestsAdapter
    }

    private fun displayProfile(profile: ProfileDto) {
        GlideApp.with(this)
                .load(profile.image?.original)
                .into(ivProfile)
        tvNameAndAge.text = if (profile.age == null) {
            profile.fullName
        } else {
            getString(R.string.profile_label_name_with_age, profile.fullName, profile.age)
        }
        if (profile.designation.isNullOrBlank() || profile.company.isNullOrBlank()) {
            tvDesignation.gone()
        } else {
            tvDesignation.visible()
            tvDesignation.text = getString(R.string.profile_designation_at_company, profile.designation, profile.company)
        }

        tvFollowersCount.text = (profile.followersCount ?: 0).toString()
        tvFollowingCount.text = (profile.followingCount ?: 0).toString()

        if (profile.bio.isNullOrBlank()) {
            tvLabelBio.gone()
            tvBio.gone()
        } else {
            tvLabelBio.visible()
            tvBio.visible()
            tvBio.text = profile.bio
        }

        interestsAdapter.displayInterests(profile.interests ?: emptyList())
    }

    private fun observeChanges() {
        viewModel.logout.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    requireActivity().startLandingWithClear()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(true)
        fragment.setTargetFragment(this, AppConstants.REQ_CODE_CHOOSE_INTERESTS)
        fragmentManager?.apply {
            beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out,
                            R.anim.slide_down_in, R.anim.slide_down_out)
                    .add(android.R.id.content, fragment, ChooseInterestsFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQ_CODE_CHOOSE_INTERESTS && resultCode == Activity.RESULT_OK) {
            viewModel.profileUpdated()
            displayProfile(viewModel.getProfile())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}