package com.ribbit.ui.venues.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.local.models.VenueFilters
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.createvenue.CreateVenueActivity
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.main.explore.VenuesModeNavigator
import com.ribbit.ui.venues.VenuesViewModel
import com.ribbit.ui.venues.filters.VenueFiltersActivity
import com.ribbit.ui.venues.join.JoinVenueActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VenuesViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        venuesListAdapter = VenuesListAdapter(glide = GlideApp.with(this),
                callback = this,
                ownProfile = viewModel.getOwnProfile())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        observeChanges()
        setupVenuesRecycler()
    }

    override fun onStart() {
        super.onStart()
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
    }

    private fun setupVenuesRecycler() {
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
        // Open own venue
        if (venue.isMember == true) {
            val intent = ChatActivity.getStartIntent(requireActivity(), venue, AppConstants.REQ_CODE_VENUE_CHAT)
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
            return
        }

        // Join other venue
        val intent = JoinVenueActivity.getStartIntent(requireActivity(), venue)
        startActivityForResult(intent, AppConstants.REQ_CODE_JOIN_VENUE)
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
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val venue = data?.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
                    if (venue != null && venue.isMember == false) {
                        // Only valid if user has exit a venue
                        venuesListAdapter.removeVenue(venue)

                        // Set displayed child to no venues if venues count is 0
                        if (venuesListAdapter.getVenuesCount() == 0) {
                            viewSwitcher.displayedChild = CHILD_NO_VENUES
                        }
                    }
                    getVenues(false)
                }
            }

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

            AppConstants.REQ_CODE_JOIN_VENUE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val venue = data?.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
                    if (venue != null) {
                        if (venue.isPrivate == true) {
                            // Update venue if joined venue was private
                            venuesListAdapter.updateVenueJoinedStatus(venue)
                        } else {
                            // Open the joined venue chat if venue is public
                            val intent = ChatActivity.getStartIntent(requireActivity(), venue, AppConstants.REQ_CODE_VENUE_CHAT)
                            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                            getVenues(false)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }
}