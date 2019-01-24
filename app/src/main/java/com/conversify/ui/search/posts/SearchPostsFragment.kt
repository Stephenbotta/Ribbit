package com.conversify.ui.search.posts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_posts.*


class SearchPostsFragment : BaseFragment(), SearchPostAdapter.Callback {

    companion object {
        const val TAG = "SearchPostsFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_posts

    private lateinit var viewModel: SearchPostViewModel
    private lateinit var adapter: SearchPostAdapter
    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchPostViewModel::class.java)
        adapter = SearchPostAdapter(GlideApp.with(this), this)
        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getPostSearch()
    }

    private fun observeChanges() {
        viewModel.postSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
//                    items.add(YourVenuesDto)
                    items.addAll(data)
                    if (firstPage) {
                        adapter.displayItems(items)
                    } else {
                        adapter.addMoreItems(items)
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })

    }

    private fun getPostSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getPostSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
//        staggeredGridLayoutManager.gapStrategy=GAP_HANDLING_NONE
        rvPostSearch.layoutManager = staggeredGridLayoutManager
        rvPostSearch.adapter = adapter
        (rvPostSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvPostSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getPostSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getPostSearch()
    }

    override fun onClick(post: GroupPostDto) {
    }
}