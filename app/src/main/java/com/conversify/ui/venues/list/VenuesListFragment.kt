package com.conversify.ui.venues.list

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.local.models.VenueFilters
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.createvenue.CreateVenueActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.main.explore.VenuesModeNavigator
import com.conversify.ui.venues.VenuesViewModel
import com.conversify.ui.venues.filters.VenueFiltersActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_venues_list.*

class VenuesListFragment : BaseFragment(), VenuesListAdapter.Callback {
    companion object {
        const val TAG = "VenuesListFragment"

        private const val CHILD_VENUES = 0
        private const val CHILD_NO_VENUES = 1
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_list

    private lateinit var viewModel: VenuesViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var venuesListAdapter: VenuesListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VenuesViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        setListeners()
        observeChanges()
        setupVenuesRecycler()
        getVenues()
    }

    private fun setListeners() {
        fabCreateVenue.setOnClickListener {
            val intent = Intent(requireActivity(), CreateVenueActivity::class.java)
            startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_VENUE)
        }

        btnMapVenues.setOnClickListener {
            showMapVenuesFragment()
        }
        btnVenuesFilter.setOnClickListener {
            val intent = VenueFiltersActivity.getStartIntent(requireActivity(), viewModel.getFilters())
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_FILTERS)
        }
        swipeRefreshLayout.setOnRefreshListener { getVenues() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.searchListVenues(query ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    private fun observeChanges() {
        viewModel.listVenues.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val items = resource.data ?: emptyList()
                    venuesListAdapter.displayItems(items)
                    swipeRefreshLayout.isRefreshing = false

                    viewSwitcher.displayedChild = if (venuesListAdapter.itemCount == 0) {
                        CHILD_NO_VENUES
                    } else {
                        CHILD_VENUES
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> swipeRefreshLayout.isRefreshing = true
            }
        })

        viewModel.joinVenue.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)

                    resource.data?.let { venue ->
                        if (venue.isPrivate == true) {
                            requireActivity().longToast(R.string.venues_message_notification_sent_to_admin)
                        } else {
                            // Open the joined venue chat if venue is public
                            ChatActivity.start(requireActivity(), venue)
                            getVenues(false)
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

    private fun setupVenuesRecycler() {
        venuesListAdapter = VenuesListAdapter(GlideApp.with(this), this)
        rvVenues.adapter = venuesListAdapter
    }

    private fun getVenues(showLoading: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getListVenues(showLoading)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onVenueClicked(venue: VenueDto) {
        // Open own venues
        if (venue.myVenue) {
            ChatActivity.start(requireActivity(), venue)
            return
        }

        // Join other venues
        if (isNetworkActiveWithMessage()) {
            viewModel.joinVenue(venue)
        }
    }

    private fun showMapVenuesFragment() {
        val parentFragment = parentFragment
        if (parentFragment is VenuesModeNavigator) {
            parentFragment.navigateToVenuesMap()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_CREATE_VENUE -> {
                if (resultCode == Activity.RESULT_OK) {
                    getVenues(false)
                }
            }

            AppConstants.REQ_CODE_VENUE_FILTERS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val filters = data?.getParcelableExtra<VenueFilters>(AppConstants.EXTRA_VENUE_FILTERS)
                    viewModel.updateFilters(filters)
                    viewModel.getListVenues(true)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}