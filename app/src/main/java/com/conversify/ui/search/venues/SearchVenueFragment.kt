package com.conversify.ui.search.venues

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_venue.*


class SearchVenueFragment : BaseFragment(), SearchVenueAdapter.Callback {

    companion object {
        const val TAG = "SearchVenueFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_venue

    private lateinit var viewModel: SearchVenueViewModel
    private lateinit var adapter: SearchVenueAdapter
    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchVenueViewModel::class.java)
        adapter = SearchVenueAdapter(GlideApp.with(this), this)
        loadingDialog = LoadingDialog(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeChanges()
        setupHomeRecycler()
        getVenueSearch()
    }

    private fun observeChanges() {
        viewModel.venueSearch.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    val group = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
                    items.add(YourVenuesDto)
                    items.addAll(group)
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

    private fun getVenueSearch(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getVenueSearch(showLoading, search)
        }
    }

    private fun setupHomeRecycler() {
        rvVenueSearch.adapter = adapter
        (rvVenueSearch.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvVenueSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && viewModel.validForPaging() && isNetworkActive()) {
                    viewModel.getVenueSearch(false, search)
                }
            }
        })
    }

    fun search(query: String) {
        this.search = query
        getVenueSearch()
    }

    override fun onClick(venue: VenueDto) {

    }
}