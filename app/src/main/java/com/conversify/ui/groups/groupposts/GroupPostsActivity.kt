package com.conversify.ui.groups.groupposts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_group_posts.*

class GroupPostsActivity : BaseActivity(), GroupPostsAdapter.Callback {
    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val CHILD_POSTS = 0
        private const val CHILD_NO_POSTS = 1

        fun start(context: Context, group: GroupDto) {
            context.startActivity(Intent(context, GroupPostsActivity::class.java)
                    .putExtra(EXTRA_GROUP, group))
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[GroupPostsViewModel::class.java] }
    private val group by lazy { intent.getParcelableExtra<GroupDto>(EXTRA_GROUP) }
    private lateinit var postsAdapter: GroupPostsAdapter
    private var groupPostsLoadedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_posts)

        viewModel.start(group)

        btnBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.setOnRefreshListener { getGroupPosts() }
        setupPostsRecycler()
        displayGroupDetails(group)
        observeChanges()
        getGroupPosts()
    }

    private fun setupPostsRecycler() {
        postsAdapter = GroupPostsAdapter(GlideApp.with(this), this)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_recycler)
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable)
        }
        rvPosts.addItemDecoration(dividerItemDecoration)

        rvPosts.adapter = postsAdapter
        rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getGroupPosts(false)
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
        viewModel.posts.observe(this, Observer { resource ->
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
            viewModel.getGroupPosts(firstPage)
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
}