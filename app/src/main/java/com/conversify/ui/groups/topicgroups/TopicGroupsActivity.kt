package com.conversify.ui.groups.topicgroups

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.groups.groupposts.GroupPostsActivity
import com.conversify.ui.groups.listing.GroupsViewModel
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_topic_groups.*

class TopicGroupsActivity : BaseActivity() {
    companion object {
        private const val EXTRA_TOPIC = "EXTRA_TOPIC"
        private const val CHILD_GROUPS = 0
        private const val CHILD_NO_GROUPS = 1

        fun getStartIntent(context: Context, topic: InterestDto): Intent {
            return Intent(context, TopicGroupsActivity::class.java)
                    .putExtra(EXTRA_TOPIC, topic)
        }
    }

    private lateinit var topicGroupsViewModel: TopicGroupsViewModel
    private lateinit var groupsViewModel: GroupsViewModel
    private lateinit var groupsAdapter: TopicGroupsAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_groups)

        val topic = intent.getParcelableExtra<InterestDto>(EXTRA_TOPIC)
        topicGroupsViewModel = ViewModelProviders.of(this)[TopicGroupsViewModel::class.java]
        groupsViewModel = ViewModelProviders.of(this)[GroupsViewModel::class.java]
        topicGroupsViewModel.start(topic)

        loadingDialog = LoadingDialog(this)

        tvTopic.text = topic.name
        btnBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.isEnabled = false

        setupGroupsRecycler()
        observeChanges()
        getGroups()
    }

    private fun setupGroupsRecycler() {
        groupsAdapter = TopicGroupsAdapter(GlideApp.with(this)) { group ->
            if (group.isMember == true) {
                GroupPostsActivity.start(this, group)
            } else if (isNetworkActiveWithMessage()) {
                groupsViewModel.joinGroup(group)
            }
        }
        rvGroups.adapter = groupsAdapter
        rvGroups.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvGroups.canScrollVertically(1) && topicGroupsViewModel.validForPaging() && isNetworkActive()) {
                    topicGroupsViewModel.getGroups(false)
                }
            }
        })
    }

    private fun observeChanges() {
        topicGroupsViewModel.groups.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val groups = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        groupsAdapter.displayGroups(groups)
                    } else {
                        groupsAdapter.addGroups(groups)
                    }

                    viewSwitcher.displayedChild = if (groupsAdapter.itemCount == 0) {
                        CHILD_NO_GROUPS
                    } else {
                        CHILD_GROUPS
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

        groupsViewModel.joinGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    resource.data?.let { group ->
                        if (group.isPrivate == true) {
                            longToast(R.string.venues_message_notification_sent_to_admin)
                        } else {
                            GroupPostsActivity.start(this, group)
                            groupsAdapter.updateGroup(group)
                            setResult(Activity.RESULT_OK)
                        }
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> loadingDialog.setLoading(true)
            }
        })
    }

    private fun getGroups(firstPage: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            topicGroupsViewModel.getGroups(firstPage)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }
}