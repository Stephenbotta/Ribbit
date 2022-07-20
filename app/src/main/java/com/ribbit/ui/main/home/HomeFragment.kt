package com.ribbit.ui.main.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.post.details.PostDetailsActivity
import com.ribbit.ui.post.details.PostDetailsViewModel
import com.ribbit.ui.post.newpost.NewPostActivity
import com.ribbit.ui.profile.ProfileActivity
import com.ribbit.ui.profile.followerandfollowing.FollowerAndFollowingActivity
import com.ribbit.ui.search.SearchActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
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
        (rvHome.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvHome.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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

    override fun onHomeNotificationClicked() {
        ProfileActivity.start(requireContext(), false)
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), post, focusReplyEditText)
        startActivityForResult(intent, AppConstants.REQ_CODE_POST_DETAILS)
    }

    override fun onPostMediaClicked(post: GroupPostDto, focusReplyEditText: Boolean, media: ImageUrlDto?) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), post, focusReplyEditText, media)
        startActivityForResult(intent, AppConstants.REQ_CODE_MEDIA_DETAIL)
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
        if (profile.id == UserManager.getUserId()) {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(requireContext(), data,
                    AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
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
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.REQ_CODE_NEW_POST, AppConstants.REQ_CODE_MEDIA_DETAIL -> {
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