package com.conversify.ui.venues.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.createvenue.CreateVenueActivity
import com.conversify.ui.main.explore.VenuesModeNavigator
import com.conversify.ui.venues.VenuesViewModel
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_venues_list.*

class VenuesListFragment : BaseFragment() {
    companion object {
        const val TAG = "VenuesListFragment"

        private const val CHILD_VENUES = 0
        private const val CHILD_NO_VENUES = 1
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_list

    private lateinit var viewModel: VenuesViewModel
    private lateinit var venuesListAdapter: VenuesListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VenuesViewModel::class.java]
        setListeners()
        observeChanges()
        setupVenuesRecycler()
        getVenues()
    }

    private fun setListeners() {
        fabAddVenue.setOnClickListener {
            startActivity(Intent(requireActivity(), CreateVenueActivity::class.java))
        }

        btnMapVenues.setOnClickListener {
            showMapVenuesFragment()
        }
        btnVenuesFilter.setOnClickListener { }
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
    }

    private fun setupVenuesRecycler() {
        venuesListAdapter = VenuesListAdapter(GlideApp.with(this))
        rvVenues.adapter = venuesListAdapter
    }

    private fun getVenues() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getListVenues()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showMapVenuesFragment() {
        val parentFragment = parentFragment
        if (parentFragment is VenuesModeNavigator) {
            parentFragment.navigateToVenuesMap()
        }
    }
}