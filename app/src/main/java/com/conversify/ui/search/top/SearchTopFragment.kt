package com.conversify.ui.search.top

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_top.*


class SearchTopFragment : BaseFragment(), SearchTopAdapter.Callback {

    companion object {
        const val TAG = "SearchTopFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_top

    private lateinit var viewModel: SearchTopViewModel
    private lateinit var adapter: SearchTopAdapter
    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchTopViewModel::class.java)
        adapter = SearchTopAdapter(GlideApp.with(this), this)
        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getTopSearch()
    }

    private fun observeChanges() {
        viewModel.groups.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val profile = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
                    items.add(YourVenuesDto)
                    items.addAll(profile)
                    if (firstPage) {
                        adapter.displayItems(items)
                    } else {
                        adapter.addMoreItems(items)
                    }
//
//                    viewSwitcher.displayedChild = if (groupsAdapter.itemCount == 0) {
//                        TopicGroupsActivity.CHILD_NO_GROUPS
//                    } else {
//                        TopicGroupsActivity.CHILD_GROUPS
//                    }
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

    private fun getTopSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getTopSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvTopSearch.adapter = adapter
        (rvTopSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvTopSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getTopSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getTopSearch()
    }

    override fun onClick(profile: ProfileDto) {

    }
}