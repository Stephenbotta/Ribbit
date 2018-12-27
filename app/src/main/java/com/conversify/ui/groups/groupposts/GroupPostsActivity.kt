package com.conversify.ui.groups.groupposts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.groups.PostCallback
import com.conversify.ui.post.details.PostDetailsActivity
import com.conversify.ui.post.details.PostDetailsViewModel
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_group_posts.*
import timber.log.Timber

class GroupPostsActivity : BaseActivity(), PostCallback {
    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val CHILD_POSTS = 0
        private const val CHILD_NO_POSTS = 1

        fun start(context: Context, group: GroupDto) {
            context.startActivity(Intent(context, GroupPostsActivity::class.java)
                    .putExtra(EXTRA_GROUP, group))
        }
    }

    private val groupPostsViewModel by lazy { ViewModelProviders.of(this)[GroupPostsViewModel::class.java] }
    private val postDetailsViewModel by lazy { ViewModelProviders.of(this)[PostDetailsViewModel::class.java] }
    private val group by lazy { intent.getParcelableExtra<GroupDto>(EXTRA_GROUP) }
    private lateinit var postsAdapter: GroupPostsAdapter
    private var groupPostsLoadedOnce = false

    private val postUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(AppConstants.EXTRA_GROUP_POST)) {
                val updatedPost = intent.getParcelableExtra<GroupPostDto>(AppConstants.EXTRA_GROUP_POST)
                postsAdapter.updatePost(updatedPost)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_posts)

        groupPostsViewModel.start(group)

        btnBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.setOnRefreshListener { getGroupPosts() }

        registerPostUpdatedReceiver()
        setupPostsRecycler()
        displayGroupDetails(group)
        observeChanges()
        getGroupPosts()
    }

    private fun registerPostUpdatedReceiver() {
        val filter = IntentFilter()
        filter.addAction(AppConstants.ACTION_GROUP_POST_UPDATED_POST_DETAILS)
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(postUpdatedReceiver, filter)
    }

    private fun setupPostsRecycler() {
        postsAdapter = GroupPostsAdapter(GlideApp.with(this), this)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_recycler)
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable)
        }
        rvPosts.addItemDecoration(dividerItemDecoration)
        (rvPosts.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvPosts.adapter = postsAdapter
        rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && groupPostsViewModel.validForPaging() && isNetworkActive()) {
                    groupPostsViewModel.getGroupPosts(false)
                }
            }
        })
    }

    private fun displayGroupDetails(group: GroupDto) {
        tvTitle.text = group.name
        ivFavourite.setImageResource(if (group.isMember == true) {
            R.drawable.ic_star_selected
        } else {
            R.drawable.ic_star_normal
        })
    }

    private fun observeChanges() {
        groupPostsViewModel.posts.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    sendGroupPostsLoadedBroadcast()
                    swipeRefreshLayout.isRefreshing = false
                    val posts = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        postsAdapter.displayPosts(posts)
                    } else {
                        postsAdapter.addPosts(posts)
                    }

                    viewSwitcher.displayedChild = if (postsAdapter.itemCount == 0) {
                        CHILD_NO_POSTS
                    } else {
                        CHILD_POSTS
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
    }

    private fun getGroupPosts(firstPage: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            groupPostsViewModel.getGroupPosts(firstPage)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun sendGroupPostsLoadedBroadcast() {
        if (!groupPostsLoadedOnce) {
            groupPostsLoadedOnce = true
            val intent = Intent(AppConstants.ACTION_GROUP_POSTS_LOADED)
            intent.putExtra(AppConstants.EXTRA_GROUP, group)
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent)
        }
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        Timber.i("User profile clicked : $profile")
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(this, post, focusReplyEditText)
        startActivity(intent)
    }

    override fun onLikesCountClicked(post: GroupPostDto) {
        Timber.i("Likes count clicked for post : $post")
    }

    override fun onGroupPostLikeClicked(groupPost: GroupPostDto, isLiked: Boolean) {
        postDetailsViewModel.likeUnlikePost(groupPost, isLiked)
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(AppConstants.ACTION_GROUP_POST_UPDATED_GROUP_POSTS_LISTING)
                        .putExtra(AppConstants.EXTRA_GROUP_POST, groupPost))
    }

    override fun onHashtagClicked(tag: String) {
        Timber.i("Hash tag clicked : $tag")
    }

    override fun onUsernameMentionClicked(username: String) {
        Timber.i("Username mention clicked : $username")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(postUpdatedReceiver)
    }
}