package com.pulse.ui.search.posts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.pulse.R
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.GroupPostDto
import com.pulse.extensions.handleError
import com.pulse.extensions.isNetworkActive
import com.pulse.extensions.isNetworkActiveWithMessage
import com.pulse.ui.base.BaseFragment
import com.pulse.ui.post.details.PostDetailsActivity
import com.pulse.utils.AppConstants
import com.pulse.utils.GlideApp
import com.pulse.utils.SpannedGridLayoutManager
import kotlinx.android.synthetic.main.fragment_search_posts.*


class SearchPostsFragment : BaseFragment(), SearchPostAdapter.Callback {

    companion object {
        const val TAG = "SearchPostsFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_posts

    private lateinit var viewModel: SearchPostViewModel
    private lateinit var adapter: SearchPostAdapter
    //    private var manager: SpannedGridLayoutManager? = null
    //    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchPostViewModel::class.java)
        observeChanges()
        setupHomeRecycler()
        getPostSearch()
    }

    private fun observeChanges() {
        viewModel.postSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
//                    loadingDialog.setLoading(false)
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true

                    if (firstPage) {
                        adapter.displayItems(data)
                    } else {
                        adapter.addMoreItems(data)
                    }
                }

                Status.ERROR -> {
//                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
//                    loadingDialog.setLoading(true)
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
        adapter = SearchPostAdapter(GlideApp.with(this), this)

        val manager = SpannedGridLayoutManager(
                object : SpannedGridLayoutManager.GridSpanLookup {
                    override fun getSpanInfo(position: Int): SpannedGridLayoutManager.SpanInfo {
                        return if (position % 6 == 0 || position % 6 == 4) {
                            SpannedGridLayoutManager.SpanInfo(2, 2)
                        } else {
                            SpannedGridLayoutManager.SpanInfo(1, 1)
                        }
                    }
                },
                3 /* Three columns */,
                1f /* We want our items to be 1:1 ratio */)

        rvPostSearch.layoutManager = manager
        rvPostSearch.adapter = adapter
        (rvPostSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvPostSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging()
                        && isNetworkActive()) {
                    viewModel.getPostSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getPostSearch()
    }

    override fun onClick(position: Int, post: GroupPostDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), item, true)
        startActivityForResult(intent, AppConstants.REQ_CODE_POST_DETAILS)
    }
}