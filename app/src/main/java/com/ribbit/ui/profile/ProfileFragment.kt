package com.ribbit.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.ribbit.ui.profile.edit.EditProfileActivity
import com.ribbit.ui.profile.followerandfollowing.FollowerAndFollowingActivity
import com.ribbit.ui.profile.settings.SettingsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment(), ProfileInterestsAdapter.Callback, View.OnClickListener {
    companion object {
        const val TAG = "ProfileFragment"
        const val ARGUMENT_FROM_TAB = "ARGUMENT_FROM_TAB"

        fun newInstance(fromTab: Boolean): ProfileFragment {
            val profileFragment = ProfileFragment()
            val bundle = Bundle()
            bundle.putBoolean(ARGUMENT_FROM_TAB, fromTab)
            profileFragment.arguments = bundle
            return profileFragment
        }
    }

    private val fromTab by lazy { arguments?.getBoolean(ARGUMENT_FROM_TAB) ?: true }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_profile

    private val viewModel by lazy { ViewModelProviders.of(this)[ProfileViewModel::class.java] }
    private lateinit var interestsAdapter: ProfileInterestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInterestsRecycler()
        swipeRefreshLayout.setOnRefreshListener { getUserProfile() }
        observeChanges()
        listener()

        if (fromTab) {
            tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back, 0, 0, 0)
        }
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

        /*tvNameAndAge.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                if (profile.userType == ApiConstants.TYPE_STUDENT) R.drawable.ic_student_symbol
                else R.drawable.ic_teacher_symbol, 0)*/
//        if (profile.designation.isNullOrBlank() || profile.company.isNullOrBlank()) {
        if (profile.designation.isNullOrBlank()) {
            tvDesignation.gone()
        } else {
            tvDesignation.visible()
//            tvDesignation.text = getString(R.string.profile_label_designation_at_company, profile.designation, profile.company)
            tvDesignation.text = profile.designation
        }

        tvFollowersCount.text = (profile.followersCount ?: 0).toString()
//        tvFollowingCount.text = (profile.followingCount ?: 0).toString()

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
                    displayProfile(resource.data ?: ProfileDto())
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
    }

    private fun listener() {
        tvTitle.setOnClickListener(this)
        fabEdit.setOnClickListener(this)
        btnSettings.setOnClickListener(this)
        tvFollowersCount.setOnClickListener(this)
        /*tvLabelFollowers.setOnClickListener(this)
        tvFollowingCount.setOnClickListener(this)
        tvLabelFollowing.setOnClickListener(this)*/
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
        startActivity(intent)
    }

    private fun settings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(true,
                interest = viewModel.getProfile().interests ?: arrayListOf())
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

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.tvTitle -> activity?.onBackPressed()

            R.id.fabEdit -> editProfile()

            R.id.btnSettings -> settings()

            R.id.tvFollowersCount/*, R.id.tvLabelFollowers*/ -> startActivity(FollowerAndFollowingActivity.getIntentStart(requireActivity(), ApiConstants.FLAG_FOLLOWERS))

//            R.id.tvFollowingCount, R.id.tvLabelFollowing -> startActivity(FollowerAndFollowingActivity.getIntentStart(requireActivity(), ApiConstants.FLAG_FOLLOWINGS))

        }

    }

    private fun visible() {
        ivProfile.visibility = View.VISIBLE
        fabEdit.visible()
        tvNameAndAge.visibility = View.VISIBLE
        tvDesignation.visibility = View.VISIBLE
        /*viewDividerFollowersTop.visibility = View.VISIBLE
        viewDividerFollowersCenter.visibility = View.VISIBLE*/
        tvFollowersCount.visibility = View.VISIBLE
        /*tvLabelFollowers.visibility = View.VISIBLE
        tvFollowingCount.visibility = View.VISIBLE
        tvLabelFollowing.visibility = View.VISIBLE*/
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
        /*viewDividerFollowersTop.visibility = View.GONE
        viewDividerFollowersCenter.visibility = View.GONE*/
        tvFollowersCount.visibility = View.GONE
        /*tvLabelFollowers.visibility = View.GONE
        tvFollowingCount.visibility = View.GONE
        tvLabelFollowing.visibility = View.GONE*/
        viewDividerFollowersBottom.visibility = View.GONE
        tvLabelBio.visibility = View.GONE
        tvBio.visibility = View.GONE
        tvLabelMyInterests.visibility = View.GONE
        rvInterests.visibility = View.GONE
    }

}