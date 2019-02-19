package com.conversify.ui.people.details

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import com.conversify.R
import com.conversify.data.local.PrefsManager
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.extensions.gone
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.chat.ChatActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_people_details.*

class PeopleDetailsActivity : BaseActivity(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        private const val EXTRA_CROSSED_PEOPLE_DETAILS = "EXTRA_CROSSED_PEOPLE_DETAILS"

        fun getStartIntent(context: Context, userCrossed: UserCrossedDto, flag: Int): Intent {
            return Intent(context, PeopleDetailsActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_CROSSED_PEOPLE_DETAILS, userCrossed)
        }

    }

    private lateinit var viewModel: PeopleDetailsViewModel
    private lateinit var interestsAdapter: PeopleMutualInterestsAdapter
    private lateinit var userId: String
    private lateinit var userCrossed: UserCrossedDto
    private var profile: ProfileDto? = null
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_details)
        inItClasses()
    }

    private fun inItClasses() {
        viewModel = ViewModelProviders.of(this).get(PeopleDetailsViewModel::class.java)
        flag = intent.getIntExtra(EXTRA_FLAG, 0)
        userCrossed = intent.getParcelableExtra<UserCrossedDto>(EXTRA_CROSSED_PEOPLE_DETAILS)
        userId = PrefsManager.get().getString(PrefsManager.PREF_PEOPLE_USER_ID, "")
        swipeRefreshLayout.setOnRefreshListener { getPeopleDetails(userId) }
        observeChanges()
        listener()
        setupInterestsRecycler()
        gone()
        getPeopleDetails(userId)
    }

    private fun listener() {
        tvBack.setOnClickListener(this)
        btnMore.setOnClickListener(this)
        tvFollowedStatus.setOnClickListener(this)
        fabChat.setOnClickListener(this)
        tvFollowersCount.setOnClickListener(this)
        tvLabelFollowers.setOnClickListener(this)
        tvFollowingCount.setOnClickListener(this)
        tvLabelFollowing.setOnClickListener(this)
    }

    private fun observeChanges() {
        viewModel.peopleDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    visible()
                    swipeRefreshLayout.isRefreshing = false
                    profile = resource.data
                    setData(profile)
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

        viewModel.followUnFollow.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    // Ignored
                }

                Status.ERROR -> {
                    // Ignored
//                    handleError(resource.error)
//                    toggleFollow()
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })

        viewModel.block.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    // Ignored
                }

                Status.ERROR -> {
                    // Ignored
//                    handleError(resource.error)
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                    // Ignored
                }
            }
        })
    }

    private fun setData(profile: ProfileDto?) {
        userCrossed.conversationId = profile?.conversationId
        val isCount = profile?.isFollowing ?: false
        if (isCount) {
            tvFollowedStatus.setText(R.string.people_detail_button_un_follow)
        } else {
            tvFollowedStatus.setText(R.string.people_detail_button_follow)
        }

        GlideApp.with(this)
                .load(profile?.image?.original)
                .into(ivProfile)

        tvNameAndAge.text = if (profile?.age == null) {
            profile?.fullName
        } else {
            getString(R.string.profile_label_name_with_age, profile.fullName, profile.age)
        }
        if (profile?.designation.isNullOrBlank() || profile?.company.isNullOrBlank()) {
            tvDesignation.gone()
        } else {
            tvDesignation.visible()
            tvDesignation.text = getString(R.string.profile_label_designation_at_company, profile?.designation, profile?.company)
        }

        tvFollowersCount.text = (profile?.followersCount ?: 0).toString()
        tvFollowingCount.text = (profile?.followingCount ?: 0).toString()

        if (profile?.bio.isNullOrBlank()) {
            tvLabelBio.gone()
            tvBio.gone()
        } else {
            tvLabelBio.visible()
            tvBio.visible()
            tvBio.text = profile?.bio
        }

        interestsAdapter.displayMutualInterests(profile?.interests ?: emptyList())
    }

    private fun toggleFollow(): Double {

        profile?.isFollowing = profile?.isFollowing?.not()
        val action = if (profile?.isFollowing == true) {
            tvFollowedStatus.setText(R.string.people_detail_button_un_follow)
            profile?.followersCount = profile?.followersCount?.inc()
            1.0
        } else {
            tvFollowedStatus.setText(R.string.people_detail_button_follow)
            profile?.followersCount = profile?.followersCount?.dec()
            2.0
        }
        tvFollowersCount?.text = profile?.followersCount?.toString()
        return action
    }

    private fun toggleBlock(): Double {
        profile?.isBlocked = profile?.isBlocked?.not()
        return if (profile?.isBlocked == true) {
            1.0
        } else {
            2.0
        }
    }

    private fun setupInterestsRecycler() {
        interestsAdapter = PeopleMutualInterestsAdapter()
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexWrap = FlexWrap.WRAP
        rvMutualInterests.layoutManager = layoutManager
        rvMutualInterests.isNestedScrollingEnabled = false
        rvMutualInterests.adapter = interestsAdapter
    }

    private fun getPeopleDetails(userId: String) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getOtherUserProfileDetails(userId)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun visible() {
        ivProfile.visibility = View.VISIBLE
        tvFollowedStatus.visibility = View.VISIBLE
        fabChat.visible()
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
        tvLabelMutualInterests.visibility = View.VISIBLE
        rvMutualInterests.visibility = View.VISIBLE
    }

    private fun gone() {
        ivProfile.visibility = View.GONE
        tvFollowedStatus.visibility = View.GONE
        fabChat.gone()
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
        tvLabelMutualInterests.visibility = View.GONE
        rvMutualInterests.visibility = View.GONE
    }

    @SuppressLint("RestrictedApi")
    private fun optionMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.menu_block_user)
        if (profile != null)
            if (profile?.isBlocked!!) {
                popup.menu.getItem(0).title = getString(R.string.people_detail_more_label_unblock)
            } else {
                popup.menu.getItem(0).title = getString(R.string.people_detail_more_label_block)
            }
//        val m = popup.menu as MenuBuilder     //  visible the icon for menu
//        m.setOptionalIconsVisible(true)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.tvBack -> {
                onBackPressed()
            }

            R.id.tvFollowedStatus -> {
                viewModel.postFollowUnFollow(userId, toggleFollow())
            }

            R.id.fabChat -> when (flag) {
                AppConstants.REQ_CODE_PEOPLE -> navigateToChat(AppConstants.REQ_CODE_INDIVIDUAL_CHAT)
                AppConstants.REQ_CODE_BLOCK_USER -> navigateToChat(AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
            }

            R.id.btnMore -> optionMenu(v)
        }
    }

    private fun navigateToChat(flag: Int) {
        val intent = ChatActivity.getStartIntentForIndividualChat(this, userCrossed, flag)
        startActivityForResult(intent, flag)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.menuBlock -> {
                viewModel.postBlock(profile?.id!!, toggleBlock())
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}