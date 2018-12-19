package com.conversify.ui.main.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

class HomeFragment : BaseFragment(), HomeAdapter.Callback {
    companion object {
        const val TAG = "HomeFragment"

        private const val CHILD_POSTS = 0
        private const val CHILD_NO_POSTS = 1
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        adapter = HomeAdapter(GlideApp.with(this), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { getHomeFeed() }
        setupHomeRecycler()
        observeChanges()
        getHomeFeed()
    }

    private fun setupHomeRecycler() {
        rvHome.adapter = adapter
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

                    viewSwitcher.displayedChild = if (adapter.itemCount == 0) {
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

    private fun getHomeFeed() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getHomeFeed()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onHomeSearchClicked() {
        Timber.i("Home search clicked")
    }

    override fun onPostClicked(post: GroupPostDto) {
        Timber.i("Post clicked\n$post")
    }
}