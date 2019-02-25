package com.conversify.ui.search.venues

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.venues.join.JoinVenueActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_venue.*


class SearchVenueFragment : BaseFragment(), SearchVenueAdapter.Callback {

    companion object {
        const val TAG = "SearchVenueFragment"
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_venue

    private lateinit var viewModel: SearchVenueViewModel
    private lateinit var adapter: SearchVenueAdapter
    //    private lateinit var loadingDialog: LoadingDialog
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchVenueViewModel::class.java)
        adapter = SearchVenueAdapter(GlideApp.with(this), this)
//        loadingDialog = LoadingDialog(requireContext())
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
//                    loadingDialog.setLoading(false)
                    val data = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    val items = mutableListOf<Any>()
                    if (firstPage)
                        items.add(YourVenuesDto)
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

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val venue = data?.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
                    if (venue != null && venue.isMember == false) {
                        // Only valid if user has exit a venue
//                        venuesListAdapter.removeVenue(venue)
//
//                        // Set displayed child to no venues if venues count is 0
//                        if (venuesListAdapter.getVenuesCount() == 0) {
//                            viewSwitcher.displayedChild = VenuesListFragment.CHILD_NO_VENUES
//                        }
                    }
                    getVenueSearch()
                }
            }

            AppConstants.REQ_CODE_JOIN_VENUE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val venue = data?.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
                    if (venue != null) {
                        if (venue.isPrivate == true) {
                            // Update venue if joined venue was private
                            adapter.updateVenueJoinedStatus(venue)
                        } else {
                            // Open the joined venue chat if venue is public
                            adapter.updateVenueJoinedStatus(venue)
                            val intent = ChatActivity.getStartIntent(requireActivity(), venue, AppConstants.REQ_CODE_VENUE_CHAT)
                            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                        }
                    }
                }
            }
        }
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

    override fun onClick(position: Int, venue: VenueDto) {
        val items = adapter.getUpdatedList()
        val item = items[position]
        if (item is VenueDto) {

            // Open own venue
            if (item.isMember == true) {
                val intent = ChatActivity.getStartIntent(requireActivity(), item, AppConstants.REQ_CODE_VENUE_CHAT)
                startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                return
            }
            // Join other venue
            val intent = JoinVenueActivity.getStartIntent(requireActivity(), item)
            startActivityForResult(intent, AppConstants.REQ_CODE_JOIN_VENUE)
        }
    }
}