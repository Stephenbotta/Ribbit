package com.conversify.ui.venues

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.venues.GetVenuesResponse
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.VenuesNearYouDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenuesViewModel : ViewModel() {
    val listVenues by lazy { MutableLiveData<Resource<List<Any>>>() }
    val mapVenues by lazy { MutableLiveData<Resource<List<VenueDto>>>() }
    val joinVenue by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    private val myVenues by lazy { mutableListOf<VenueDto>() }
    private val nearbyVenues by lazy { mutableListOf<VenueDto>() }

    private var searchListQuery = ""
    private var searchMapQuery = ""

    fun getListVenues(showLoading: Boolean = true) {
        if (showLoading) {
            listVenues.value = Resource.loading()
        }

        RetrofitClient.conversifyApi
                .getVenues(latitude = UserManager.getLastLatitude(),
                        longitude = UserManager.getLastLongitude())
                .enqueue(object : Callback<ApiResponse<GetVenuesResponse>> {
                    override fun onResponse(call: Call<ApiResponse<GetVenuesResponse>>,
                                            response: Response<ApiResponse<GetVenuesResponse>>) {
                        if (response.isSuccessful) {
                            val myVenues = response.body()?.data?.myVenues ?: emptyList()
                            val nearbyVenues = response.body()?.data?.nearbyVenues ?: emptyList()

                            // Set my venue flag to true for all my listVenues
                            myVenues.forEach { it.myVenue = true }

                            this@VenuesViewModel.myVenues.clear()
                            this@VenuesViewModel.myVenues.addAll(myVenues)

                            this@VenuesViewModel.nearbyVenues.clear()
                            this@VenuesViewModel.nearbyVenues.addAll(nearbyVenues)

                            val venueItems = if (searchListQuery.isBlank()) {
                                getVenueListItems(myVenues, nearbyVenues)
                            } else {
                                getSearchVenueListResult(searchListQuery)
                            }

                            listVenues.value = Resource.success(venueItems)
                        } else {
                            listVenues.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetVenuesResponse>>, t: Throwable) {
                        listVenues.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getMapVenues() {
        mapVenues.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getVenues(latitude = UserManager.getLastLatitude(),
                        longitude = UserManager.getLastLongitude())
                .enqueue(object : Callback<ApiResponse<GetVenuesResponse>> {
                    override fun onResponse(call: Call<ApiResponse<GetVenuesResponse>>,
                                            response: Response<ApiResponse<GetVenuesResponse>>) {
                        if (response.isSuccessful) {
                            val myVenues = response.body()?.data?.myVenues ?: emptyList()
                            val nearbyVenues = response.body()?.data?.nearbyVenues ?: emptyList()

                            // Set my venue flag to true for all my listVenues
                            myVenues.forEach { it.myVenue = true }

                            this@VenuesViewModel.myVenues.clear()
                            this@VenuesViewModel.myVenues.addAll(myVenues)

                            this@VenuesViewModel.nearbyVenues.clear()
                            this@VenuesViewModel.nearbyVenues.addAll(nearbyVenues)

                            val venueItems = if (searchMapQuery.isBlank()) {
                                getVenueMapItems(myVenues, nearbyVenues)
                            } else {
                                getSearchVenueMapResult(searchMapQuery)
                            }

                            mapVenues.value = Resource.success(venueItems)
                        } else {
                            mapVenues.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetVenuesResponse>>, t: Throwable) {
                        mapVenues.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun joinVenue(venue: VenueDto) {
        joinVenue.value = Resource.loading()

        RetrofitClient.conversifyApi
                .joinVenue(venueId = venue.id ?: "",
                        adminId = venue.adminId ?: "",
                        isPrivate = venue.isPrivate ?: false)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            joinVenue.value = Resource.success(venue)
                        } else {
                            joinVenue.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        joinVenue.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun searchListVenues(query: String) {
        searchListQuery = query
        val searchResult = getSearchVenueListResult(query)
        listVenues.value = Resource.success(searchResult)
    }

    fun searchMapsVenues(query: String) {
        searchMapQuery = query
        val searchResult = getSearchVenueMapResult(query)
        mapVenues.value = Resource.success(searchResult)
    }

    /**
     * Returns venues list items in correct order after applying the search filter
     * */
    private fun getSearchVenueListResult(query: String): List<Any> {
        // If query is blank then return the result with all venues
        if (query.isBlank()) {
            return getVenueListItems(myVenues, nearbyVenues)
        }

        val myVenuesResult = myVenues.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }
        val nearbyVenuesResult = nearbyVenues.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }

        return getVenueListItems(myVenuesResult, nearbyVenuesResult)
    }

    /**
     * Returns venues list items in correct order
     * */
    private fun getVenueListItems(myVenues: List<VenueDto>, nearbyVenues: List<VenueDto>): List<Any> {
        val venueItems = mutableListOf<Any>()

        if (myVenues.isNotEmpty()) {
            venueItems.add(YourVenuesDto)
            venueItems.addAll(myVenues)
        }

        if (nearbyVenues.isNotEmpty()) {
            venueItems.add(VenuesNearYouDto())
            venueItems.addAll(nearbyVenues)
        }

        return venueItems
    }

    /**
     * Returns venues map items in correct order after applying the search filter
     * */
    private fun getSearchVenueMapResult(query: String): List<VenueDto> {
        // If query is blank then return the result with all venues
        if (query.isBlank()) {
            return getVenueMapItems(myVenues, nearbyVenues)
        }

        val myVenuesResult = myVenues.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }
        val nearbyVenuesResult = nearbyVenues.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }

        return getVenueMapItems(myVenuesResult, nearbyVenuesResult)
    }

    /**
     * Returns venues map items in correct order
     * */
    private fun getVenueMapItems(myVenues: List<VenueDto>, nearbyVenues: List<VenueDto>): List<VenueDto> {
        val venueItems = mutableListOf<VenueDto>()
        venueItems.addAll(myVenues)
        venueItems.addAll(nearbyVenues)
        return venueItems
    }
}