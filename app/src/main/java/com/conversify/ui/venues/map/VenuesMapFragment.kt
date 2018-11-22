package com.conversify.ui.venues.map

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.createvenue.CreateVenueActivity
import com.conversify.ui.custom.LoadingDialog
import com.conversify.ui.main.explore.VenuesModeNavigator
import com.conversify.ui.venues.VenuesViewModel
import com.conversify.utils.AppConstants
import com.conversify.utils.AppUtils
import com.conversify.utils.GlideApp
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_venues_map.*

class VenuesMapFragment : BaseFragment(), VenuesMapHelper.Callback {
    companion object {
        const val TAG = "VenuesMapFragment"
    }

    private lateinit var viewModel: VenuesViewModel
    private lateinit var loadingDialog: LoadingDialog
    private var mapHelper: VenuesMapHelper? = null
    private var selectedVenue: VenueDto? = null

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_venues_map

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this)[VenuesViewModel::class.java]
        loadingDialog = LoadingDialog(requireActivity())
        setupMapFragment()
        setListeners()
        observeChanges()
        getVenues()
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mapHelper = VenuesMapHelper(requireActivity(), googleMap, this)
        }
    }

    private fun setListeners() {
        btnListVenues.setOnClickListener {
            showListVenuesFragment()
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

        clSelectedVenue.setOnClickListener {
            selectedVenue?.let { venue ->
                // Open own venues
                if (venue.myVenue) {
                    ChatActivity.start(requireActivity(), venue)
                    return@setOnClickListener
                }

                // Join other venues
                if (isNetworkActiveWithMessage()) {
                    viewModel.joinVenue(venue)
                }
            }
        }

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
                    mapHelper?.displayVenues(venues)
                    onMapVenueDeselected()
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
                        } else {
                            // Open the joined venue chat if venue is public
                            ChatActivity.start(requireActivity(), venue)
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

    private fun displaySelectedVenueDetails(venue: VenueDto) {
        if (venue.isPrivate == true) {
            ivPrivate.visible()
        } else {
            ivPrivate.gone()
        }

        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = venue.name
        tvVenueLocation.text = AppUtils.getFormattedAddress(venue.locationName, venue.locationAddress)

        val memberCount = venue.memberCount ?: 0
        tvActiveMembers.text = resources.getQuantityString(R.plurals.venues_label_active_members_with_count, memberCount, memberCount)
        tvDistance.text = if (venue.distance == null) {
            ""
        } else {
            getString(R.string.distance_mile_with_value, venue.distance)
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
    }

    override fun onMapClicked() {
        onMapVenueDeselected()
    }

    override fun onMapVenueSelected(venue: VenueDto) {
        selectedVenue = venue   // Update selected venue
        displaySelectedVenueDetails(venue)
        searchView.clearFocus()
        searchView.hideKeyboard()
        clSelectedVenue.visible()
        fabCreateVenue.hide()
        llListFilters.gone()
    }

    override fun onMapVenueDeselected() {
        selectedVenue = null    // Remove selected venue reference
        clSelectedVenue.gone()
        fabCreateVenue.show()
        llListFilters.visible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQ_CODE_CREATE_VENUE && resultCode == Activity.RESULT_OK) {
            getVenues()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
        mapHelper?.clear()
    }
}