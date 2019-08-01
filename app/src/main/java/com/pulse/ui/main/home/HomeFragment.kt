package com.pulse.ui.main.home

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.pulse.R
import com.pulse.data.local.PrefsManager
import com.pulse.data.local.UserManager
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.data.remote.models.groups.GroupPostDto
import com.pulse.data.remote.models.loginsignup.InterestDto
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.data.remote.models.people.UserCrossedDto
import com.pulse.extensions.*
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.conversenearby.SelectNearByActivity
import com.pulse.ui.people.details.PeopleDetailsActivity
import com.pulse.ui.post.details.PostDetailsActivity
import com.pulse.ui.post.details.PostDetailsViewModel
import com.pulse.ui.post.newpost.NewPostActivity
import com.pulse.ui.profile.ProfileActivity
import com.pulse.ui.profile.followerandfollowing.FollowerAndFollowingActivity
import com.pulse.ui.search.SearchActivity
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

class HomeFragment : BaseFragment(), HomeAdapter.Callback {

    companion object {
        const val TAG = "HomeFragment"
    }

    private val homeViewModel by lazy { ViewModelProviders.of(this)[HomeViewModel::class.java] }
    private val postDetailsViewModel by lazy { ViewModelProviders.of(this)[PostDetailsViewModel::class.java] }
    private lateinit var adapter: HomeAdapter

    private val postUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(AppConstants.EXTRA_GROUP_POST)) {
                val updatedPost = intent.getParcelableExtra<GroupPostDto>(AppConstants.EXTRA_GROUP_POST)
                adapter.updatePost(updatedPost)
            }
        }
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = HomeAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { getHomeFeed() }
//        fabPost.setOnClickListener {

//        }
        registerPostUpdatedReceiver()
        setupHomeRecycler()
        setupGroupsFab()
        observeChanges()
        val token = UserManager.getDeviceToken()
        if (token.isNotBlank())
            homeViewModel.updateDeviceToken(token)
    }

    override fun onStart() {
        super.onStart()
        getHomeFeed()
    }

    private fun registerPostUpdatedReceiver() {
        val filter = IntentFilter()
        // Add actions for which to listen for updated post
        filter.addAction(AppConstants.ACTION_GROUP_POST_UPDATED_POST_DETAILS)
        filter.addAction(AppConstants.ACTION_GROUP_POST_UPDATED_GROUP_POSTS_LISTING)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(postUpdatedReceiver, filter)
    }

    private fun setupHomeRecycler() {
        rvHome.adapter = adapter
        (rvHome.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && homeViewModel.validForPaging() && isNetworkActive()) {
                    homeViewModel.getHomeFeed(false)
                }
            }
        })
    }

    private fun observeChanges() {
        homeViewModel.homeFeed.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val posts = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        adapter.displayItems(posts)
                    } else {
                        adapter.addItems(posts)
                    }

                    if (adapter.isEmpty()) {
                        tvNoPosts.visible()
                    } else {
                        tvNoPosts.gone()
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

        homeViewModel.updateDeviceToken.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    // Ignored
                }

                Status.ERROR -> {
                    // Ignored
//                    handleError(resource.error)
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })
    }

    private fun getHomeFeed(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            homeViewModel.getHomeFeed(showLoading = showLoading)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupGroupsFab() {
        /*val colorWhite = ContextCompat.getColor(requireActivity(), R.color.white)
        val colorPrimary = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)*/

        /* fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabAddGroup, R.drawable.ic_plus_white)
                 .setFabBackgroundColor(colorWhite)
                 .setFabImageTintColor(colorPrimary)
                 .setLabel(R.string.home_label_create_new_post)
                 .setLabelColor(colorPrimary)
                 .create())*/

        /* fabGroups.addActionItem(SpeedDialActionItem.Builder(R.id.fabTopics, R.drawable.binoculars)
                 .setFabBackgroundColor(colorWhite)
                 .setFabImageTintColor(colorPrimary)
                 .setLabel(R.string.home_label_find_someone)
                 .setLabelColor(colorPrimary)
                 .create())*/

        /*fabGroups.setOnActionSelectedListener { item ->
            return@setOnActionSelectedListener when (item.id) {
                R.id.fabAddGroup -> {
                    val intent = Intent(requireActivity(), NewPostActivity::class.java)
                    startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
                    false
                }

               R.id.fabTopics -> {
                    val intent = Intent(requireActivity(), SelectNearByActivity::class.java)
                    startActivity(intent)
                    false
                }

                else -> false
            }
        }*/

        fabPost.setOnClickListener {
            val intent = Intent(requireActivity(), NewPostActivity::class.java)
            startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
        }
    }

    override fun onHomeSearchClicked() {
        Timber.i("Home search clicked")
        val intent = SearchActivity.getStartIntent(requireContext(), AppConstants.REQ_CODE_HOME_SEARCH)
        startActivityForResult(intent, AppConstants.REQ_CODE_HOME_SEARCH)
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), post, focusReplyEditText)
        startActivityForResult(intent, AppConstants.REQ_CODE_POST_DETAILS)
    }

    override fun onLikesCountClicked(post: GroupPostDto) {
        Timber.i("Likes count clicked")
        val intent = FollowerAndFollowingActivity.getIntentStart(requireActivity(), AppConstants.REQ_CODE_POST_LIKE)
        intent.putExtra(AppConstants.EXTRA_POST_ID, post.id)
        startActivity(intent)
    }

    override fun onGroupClicked(group: GroupDto) {
        Timber.i("Group name clicked : $group")
        //GroupPostsActivity.start(requireActivity(), group)
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        Timber.i("User profile clicked : $profile")
        val data = UserCrossedDto()
        data.profile = profile
        PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, profile.id ?: "")
        if (profile.id == UserManager.getUserId()) {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(requireContext(), data, AppConstants.REQ_CODE_BLOCK_USER)
            startActivity(intent)
        }
    }

    override fun onHashtagClicked(tag: String) {
        Timber.i("Hashtag clicked : $tag")
    }

    override fun onUsernameMentionClicked(username: String) {
        Timber.i("Username mention clicked : $username")
    }

    override fun onGroupPostLikeClicked(groupPost: GroupPostDto, isLiked: Boolean) {
        postDetailsViewModel.likeUnlikePost(groupPost, isLiked)
    }

    override fun onGroupCategoryClicked(category: InterestDto) {
        //startActivity(TopicGroupsActivity.getStartIntent(requireActivity(), category))
    }

    override fun onAddCommentClicked(post: GroupPostDto, comment: String) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_NEW_POST -> {
                if (resultCode == Activity.RESULT_OK) {
                    getHomeFeed(false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireActivity())
                .unregisterReceiver(postUpdatedReceiver)
    }
}