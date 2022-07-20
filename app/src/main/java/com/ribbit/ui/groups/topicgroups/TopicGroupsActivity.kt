package com.ribbit.ui.groups.topicgroups

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActive
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.longToast
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.groups.groupposts.GroupPostsActivity
import com.ribbit.ui.groups.listing.GroupsViewModel
import com.ribbit.utils.GlideApp
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
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_groups)

        val topic = intent.getParcelableExtra<InterestDto>(EXTRA_TOPIC)
        topicGroupsViewModel = ViewModelProviders.of(this)[TopicGroupsViewModel::class.java]
        groupsViewModel = ViewModelProviders.of(this)[GroupsViewModel::class.java]
        if (topic != null) {
            topicGroupsViewModel.start(topic)
        }

        loadingDialog = LoadingDialog(this)

        if (topic != null) {
            tvTopic.text = topic.name
        }
        btnBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.isEnabled = false

        setupGroupsRecycler()
        observeChanges()
        getGroups()
    }

    private fun setupGroupsRecycler() {
        groupsAdapter = TopicGroupsAdapter(GlideApp.with(this)) { group ->
            if (group.isMember == true) {
                val intent = GroupPostsActivity.start(this, group)
                startActivity(intent)
            } else if (isNetworkActiveWithMessage()) {
                groupsViewModel.joinGroup(group)
            }
        }
        rvGroups.adapter = groupsAdapter
        rvGroups.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
                        groupsAdapter.updateGroup(group)
                        if (group.isPrivate == true) {
                            longToast(R.string.venues_message_notification_sent_to_admin)
                        } else {
                            val intent = GroupPostsActivity.start(this, group)
                            startActivity(intent)
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