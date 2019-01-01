package com.conversify.ui.venues.map

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
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.createvenue.CreateVenueActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.main.explore.VenuesModeNavigator
import com.conversify.ui.venues.VenuesViewModel
import com.conversify.ui.venues.chat.ChatActivity
import com.conversify.ui.venues.filters.VenueFiltersActivity
import com.conversify.ui.venues.list.VenuesListAdapter
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_venues_map.*

class VenuesMapFragment : BaseFragment(), VenuesMapHelper.Callback, VenuesListAdapter.Callback {
    companion object {
        const val TAG = "VenuesMapFragment"
    }

    private lateinit var viewModel: VenuesViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var selectedVenuesAdapter: VenuesListAdapter

    private var mapHelper: VenuesMapHelper? = null

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_map

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VenuesViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        llListFilters.visible()
        setupMapFragment()
        setupSelectedVenuesRecycler()
        setListeners()
        observeChanges()
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mapHelper = VenuesMapHelper(requireActivity(), googleMap, this)
        }
    }

    private fun setupSelectedVenuesRecycler() {
        selectedVenuesAdapter = VenuesListAdapter(GlideApp.with(this), this, viewModel.getOwnProfile())
        rvVenues.adapter = selectedVenuesAdapter
    }

    private fun setListeners() {
        btnListVenues.setOnClickListener {
            showListVenuesFragment()
        }

        btnVenuesFilter.setOnClickListener {
            val intent = VenueFiltersActivity.getStartIntent(requireActivity(), viewModel.getFilters())
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_FILTERS)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                viewModel.searchMapsVenues(query ?: "")
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })

        fabCreateVenue.setOnClickListener {
            val intent = Intent(requireActivity(), CreateVenueActivity::class.java)
            startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_VENUE)
        }
    }

    private fun observeChanges() {
        viewModel.mapVenues.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val venues = resource.data ?: emptyList()
                    onMapVenueDeselected()
                    mapHelper?.displayVenues(venues)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
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
                            selectedVenuesAdapter.updateVenue(venue)
                        } else {
                            // Open the joined venue chat if venue is public
                            val intent = ChatActivity.getStartIntent(requireActivity(), venue)
                            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
                            getVenues()
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

    private fun getVenues() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getMapVenues()
        }
    }

    private fun showListVenuesFragment() {
        val parentFragment = parentFragment
        if (parentFragment is VenuesModeNavigator) {
            parentFragment.navigateToVenuesList()
        }
    }

    override fun onMapLoaded(googleMap: GoogleMap) {
        // Start, top, end and bottom
        googleMap.setPadding(0, searchView.height + requireActivity().pxFromDp(20), 0, 0)
        getVenues()
    }

    override fun onMapClicked() {
        onMapVenueDeselected()
    }

    override fun onMapVenueSelected(venue: VenueDto) {
        selectedVenuesAdapter.displayItems(listOf(venue))
        searchView.clearFocus()
        searchView.hideKeyboard()
        rvVenues.visible()
        fabCreateVenue.hide()
        llListFilters.gone()
    }

    override fun onMapVenueDeselected() {
        rvVenues.gone()
        fabCreateVenue.show()
        llListFilters.visible()
    }

    override fun onVenueClicked(venue: VenueDto) {
        // Open own venue
        if (venue.isMember == true) {
            val intent = ChatActivity.getStartIntent(requireActivity(), venue)
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
            return
        }

        // Join other venue
        if (isNetworkActiveWithMessage()) {
            viewModel.joinVenue(venue)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_VENUE_CHAT,
            AppConstants.REQ_CODE_CREATE_VENUE -> {
                if (resultCode == Activity.RESULT_OK) {
                    getVenues()
                }
            }

            AppConstants.REQ_CODE_VENUE_FILTERS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val filters = data?.getParcelableExtra<VenueFilters>(AppConstants.EXTRA_VENUE_FILTERS)
                    viewModel.updateFilters(filters)
                    viewModel.getMapVenues()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
        mapHelper?.clear()
    }
}