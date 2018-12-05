package com.conversify.ui.groups.topicgroups

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
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_topic_groups.*

class TopicGroupsActivity : BaseActivity() {
    companion object {
        private const val EXTRA_TOPIC = "EXTRA_TOPIC"
        private const val CHILD_GROUPS = 0
        private const val CHILD_NO_GROUPS = 1

        fun start(context: Context, topic: InterestDto) {
            context.startActivity(Intent(context, TopicGroupsActivity::class.java)
                    .putExtra(EXTRA_TOPIC, topic))
        }
    }

    private lateinit var viewModel: TopicGroupsViewModel
    private lateinit var groupsAdapter: TopicGroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_groups)

        val topic = intent.getParcelableExtra<InterestDto>(EXTRA_TOPIC)
        viewModel = ViewModelProviders.of(this)[TopicGroupsViewModel::class.java]
        viewModel.start(topic)

        tvTopic.text = topic.name
        btnBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.isEnabled = false

        setupGroupsRecycler()
        observeChanges()
        getGroups()
    }

    private fun setupGroupsRecycler() {
        groupsAdapter = TopicGroupsAdapter(GlideApp.with(this)) { group ->
        }
        rvGroups.adapter = groupsAdapter
        rvGroups.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvGroups.canScrollVertically(1) && viewModel.validForPaging()) {
                    viewModel.getGroups(false)
                }
            }
        })
    }

    private fun observeChanges() {
        viewModel.groups.observe(this, Observer { resource ->
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
    }

    private fun getGroups(firstPage: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getGroups(firstPage)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }
}