package com.ribbit.ui.people.details

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.activity_people_details.*

class PeopleDetailsActivity : BaseActivity(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"
        private const val EXTRA_CROSSED_PEOPLE_DETAILS = "EXTRA_CROSSED_PEOPLE_DETAILS"
        private const val EXTRA_USER_ID = "EXTRA_USER_ID"

        fun getStartIntent(context: Context, userCrossed: UserCrossedDto, flag: Int, userId: String): Intent {
            return Intent(context, PeopleDetailsActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_CROSSED_PEOPLE_DETAILS, userCrossed)
                    .putExtra(EXTRA_USER_ID, userId)
        }
    }

    private lateinit var viewModel: PeopleDetailsViewModel
    private lateinit var interestsAdapter: PeopleMutualInterestsAdapter
    private var profile: ProfileDto? = null
    private val flag by lazy { intent.getIntExtra(EXTRA_FLAG, AppConstants.REQ_CODE_PEOPLE) }
    private val userId by lazy { intent.getStringExtra(EXTRA_USER_ID) ?: "" }
    private val userCrossed by lazy {
        intent.getParcelableExtra(EXTRA_CROSSED_PEOPLE_DETAILS) ?: UserCrossedDto()
    }

    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_details)
        inItClasses()
    }

    private fun inItClasses() {
        viewModel = ViewModelProviders.of(this).get(PeopleDetailsViewModel::class.java)
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
    }

    private fun observeChanges() {
        viewModel.peopleDetails.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    visible()
                    swipeRefreshLayout.isRefreshing = false
                    profile = resource.data
                    if (profile?.id == null) {
                        viewFlipper.displayedChild = 1
                        btnMore.gone()
                    } else {
                        viewFlipper.displayedChild = 0
                        visible()
                        userCrossed.profile = profile
                        setData(profile)
                    }
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

        viewModel.followUnFollow.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    if (profile?.isAccountPrivate == true)
                        getPeopleDetails(userId)
                }

                Status.ERROR -> {
                }

                Status.LOADING -> {
                }
            }
        })

        viewModel.block.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
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
            profile?.userName
        } else {
            getString(R.string.profile_label_name_with_age, profile.userName, profile.age)
        }
        /*tvNameAndAge.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                if (profile?.userType == ApiConstants.TYPE_STUDENT) R.drawable.ic_student_symbol
                else R.drawable.ic_teacher_symbol, 0)*/
        if (profile?.designation.isNullOrBlank() || profile?.company.isNullOrBlank()) {
            tvDesignation.gone()
        } else {
            tvDesignation.visible()
            tvDesignation.text = getString(R.string.profile_label_designation_at_company, profile?.designation, profile?.company)
        }

        tvFollowersCount.text = (profile?.followersCount ?: 0).toString()
//        tvFollowingCount.text = (profile?.followingCount ?: 0).toString()

        if (profile?.bio.isNullOrBlank()) {
            tvLabelBio.gone()
            tvBio.gone()
        } else {
            tvLabelBio.visible()
            tvBio.visible()
            tvBio.text = profile?.bio
        }

        if (profile?.isAccountPrivate == true) {
            ivPrivate.visible()
        } else {
            ivPrivate.gone()
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
            viewModel.getOtherUserProfileDetails(userId, flag)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun visible() {
        ivProfile.visible()
        tvFollowedStatus.visible()
        fabChat.visible()
        tvNameAndAge.visible()
        tvDesignation.visible()
        tvFollowersCount.visible()
        viewDividerFollowersBottom.visible()
        tvLabelBio.visible()
        tvBio.visible()
        tvLabelMutualInterests.visible()
        rvMutualInterests.visible()
    }

    private fun gone() {
        ivProfile.gone()
        tvFollowedStatus.gone()
        fabChat.gone()
        tvNameAndAge.gone()
        tvDesignation.gone()
        tvFollowersCount.gone()
        viewDividerFollowersBottom.gone()
        tvLabelBio.gone()
        tvBio.gone()
        tvLabelMutualInterests.gone()
        rvMutualInterests.gone()
    }

    @SuppressLint("RestrictedApi")
    private fun optionMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.menu_block_user)
        if (profile != null)
            if (profile?.isBlocked == true) {
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
                if (profile?.isRequestPending == false)
                    viewModel.postFollowUnFollow(userId, toggleFollow())
                else {
                    shortToast(getString(R.string.follow_request_text))
                }
            }

            R.id.fabChat -> when (flag) {
                AppConstants.REQ_CODE_PEOPLE -> navigateToChat(AppConstants.REQ_CODE_INDIVIDUAL_CHAT)
                AppConstants.REQ_CODE_BLOCK_USER, AppConstants.REQ_CODE_REPLY_TAG_USER -> navigateToChat(AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT)
            }

            R.id.btnMore -> optionMenu(v)
        }
    }

    private fun navigateToChat(flag: Int) {
        val intent = ChatActivity.getStartIntentForIndividualChat(this, userCrossed, flag)
        startActivityForResult(intent, flag)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {

            R.id.menuBlock -> {
                viewModel.postBlock(profile?.id ?: "", toggleBlock())
                true
            }
            else -> item?.let { super.onOptionsItemSelected(it) } == true
        }
    }

}

