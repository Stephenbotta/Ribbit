package com.conversify.ui.main.home

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.post.details.PostDetailsActivity
import com.conversify.ui.post.newpost.NewPostActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

class HomeFragment : BaseFragment(), HomeAdapter.Callback {
    companion object {
        const val TAG = "HomeFragment"
    }

    private lateinit var viewModel: HomeViewModel
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
        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        adapter = HomeAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { getHomeFeed() }
        fabPost.setOnClickListener {
            val intent = Intent(requireActivity(), NewPostActivity::class.java)
            startActivityForResult(intent, AppConstants.REQ_CODE_NEW_POST)
        }
        registerPostUpdatedReceiver()
        setupHomeRecycler()
        observeChanges()
        getHomeFeed()
    }

    private fun registerPostUpdatedReceiver() {
        val filter = IntentFilter(AppConstants.ACTION_GROUP_POSTS_UPDATED)
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(postUpdatedReceiver, filter)
    }

    private fun setupHomeRecycler() {
        rvHome.adapter = adapter
        (rvHome.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getHomeFeed(false)
                }
            }
        })
    }

    private fun observeChanges() {
        viewModel.homeFeed.observe(this, Observer { resource ->
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
    }

    private fun getHomeFeed(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getHomeFeed(showLoading = showLoading)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onHomeSearchClicked() {
        Timber.i("Home search clicked")
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), post)
        startActivityForResult(intent, AppConstants.REQ_CODE_POST_DETAILS)
    }

    override fun onLikesCountClicked(post: GroupPostDto) {
        Timber.i("Likes count clicked")
    }

    override fun onGroupClicked(group: GroupDto) {
        Timber.i("Group name clicked : $group")
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        Timber.i("User profile clicked : $profile")
    }

    override fun onHashtagClicked(tag: String) {
        Timber.i("Hashtag clicked : $tag")
    }

    override fun onUsernameMentionClicked(username: String) {
        Timber.i("Username mention clicked : $username")
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