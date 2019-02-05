package com.conversify.ui.profile

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.conversify.ui.profile.edit.EditProfileActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInterestsRecycler()
        swipeRefreshLayout.setOnRefreshListener { getUserProfile() }
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        observeChanges()
        listener()
        gone()
    }

    override fun onStart() {
        super.onStart()
        getUserProfile()
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
            tvDesignation.text = getString(R.string.profile_label_designation_at_company, profile.designation, profile.company)
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
        viewModel.peopleDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    visible()
                    swipeRefreshLayout.isRefreshing = false
                    displayProfile(viewModel.getProfile())
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                    // Ignored
                }
            }
        })

        viewModel.logout.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    requireActivity().startLandingWithClear()
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })
    }

    private fun listener() {
        tvTitle.setOnClickListener { activity?.onBackPressed() }
        fabEdit.setOnClickListener { editProfile() }
    }

    private fun getUserProfile() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getUserProfileDetails()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun editProfile() {
        val intent = Intent(requireContext(), EditProfileActivity::class.java)
        startActivityForResult(intent, 1)
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireActivity())
                .setMessage(R.string.profile_message_confirm_logout)
                .setPositiveButton(R.string.profile_btn_logout) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        viewModel.logout()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        val typeface = ResourcesCompat.getFont(requireActivity(), R.font.roboto_text_regular)
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = typeface
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
//            displayProfile(viewModel.getProfile())
            getUserProfile()
        }
    }

    private fun visible() {
        ivProfile.visibility = View.VISIBLE
        fabEdit.visible()
        tvNameAndAge.visibility = View.VISIBLE
        tvDesignation.visibility = View.VISIBLE
        viewDividerFollowersTop.visibility = View.VISIBLE
        viewDividerFollowersCenter.visibility = View.VISIBLE
        tvFollowersCount.visibility = View.VISIBLE
        tvLabelFollowers.visibility = View.VISIBLE
        tvFollowingCount.visibility = View.VISIBLE
        tvLabelFollowing.visibility = View.VISIBLE
        viewDividerFollowersBottom.visibility = View.VISIBLE
        tvLabelBio.visibility = View.VISIBLE
        tvBio.visibility = View.VISIBLE
        tvLabelMyInterests.visibility = View.VISIBLE
        rvInterests.visibility = View.VISIBLE
    }

    private fun gone() {
        ivProfile.visibility = View.GONE
        fabEdit.gone()
        tvNameAndAge.visibility = View.GONE
        tvDesignation.visibility = View.GONE
        viewDividerFollowersTop.visibility = View.GONE
        viewDividerFollowersCenter.visibility = View.GONE
        tvFollowersCount.visibility = View.GONE
        tvLabelFollowers.visibility = View.GONE
        tvFollowingCount.visibility = View.GONE
        tvLabelFollowing.visibility = View.GONE
        viewDividerFollowersBottom.visibility = View.GONE
        tvLabelBio.visibility = View.GONE
        tvBio.visibility = View.GONE
        tvLabelMyInterests.visibility = View.GONE
        rvInterests.visibility = View.GONE
    }

}