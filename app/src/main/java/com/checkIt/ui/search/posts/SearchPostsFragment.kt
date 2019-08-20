package com.checkIt.ui.search.posts

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.checkIt.R
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.extensions.handleError
import com.checkIt.extensions.isNetworkActive
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.ui.base.BaseFragment
import com.checkIt.ui.post.details.PostDetailsActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
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

        /*val manager = SpannedGridLayoutManager(
                object : SpannedGridLayoutManager.GridSpanLookup {
                    override fun getSpanInfo(position: Int): SpannedGridLayoutManager.SpanInfo {
                        return if (position % 6 == 0 || position % 6 == 4) {
                            SpannedGridLayoutManager.SpanInfo(2, 2)
                        } else {
                            SpannedGridLayoutManager.SpanInfo(1, 1)
                        }
                    }
                },
                3 *//* Three columns *//*,
                1f *//* We want our items to be 1:1 ratio *//*)*/

        val manager = SpannedGridLayoutManager(
                orientation = SpannedGridLayoutManager.Orientation.VERTICAL,
                spans = 3)

        manager.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            if (position % 6 == 0 || position % 6 == 4) {
                SpanSize(2, 2)
            } else {
                SpanSize(1, 1)
            }
        }

        rvPostSearch.layoutManager = manager
        rvPostSearch.adapter = adapter
        (rvPostSearch.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false

        rvPostSearch.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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