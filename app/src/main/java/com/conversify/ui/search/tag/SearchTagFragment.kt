package com.conversify.ui.search.tag

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_tags.*

class SearchTagFragment : BaseFragment(), SearchTagAdapter.Callback {

    companion object {
        const val TAG = "SearchTagFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_tags

    private lateinit var viewModel: SearchTagViewModel
    private lateinit var adapter: SearchTagAdapter
    //    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchTagViewModel::class.java)
        adapter = SearchTagAdapter(GlideApp.with(this), this)
//        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getTagSearch()
    }

    private fun observeChanges() {
        viewModel.tagSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
//                    loadingDialog.setLoading(false)
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
                    items.addAll(data)
                    if (firstPage) {
                        adapter.displayItems(items)
                    } else {
                        adapter.addMoreItems(items)
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

        viewModel.followUnFollow.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    // Ignored
                }

                Status.ERROR -> {
                    // Ignored
//                    handleError(resource.error)

                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })
    }

    private fun getTagSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getTagSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvTagSearch.adapter = adapter
        (rvTagSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvTagSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getTagSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getTagSearch()
    }

    override fun onClick(position: Int, profile: ProfileDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        if (item is ProfileDto) {
            profile.isFollowing = profile.isFollowing?.not()
            items.set(position, profile)
            adapter.notifyDataSetChanged()
            viewModel.postFollowUnFollowTag(profile.id!!, profile.isFollowing!!)
        }
    }
}