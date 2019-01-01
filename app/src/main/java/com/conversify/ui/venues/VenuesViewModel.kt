package com.conversify.ui.venues

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.support.annotation.StringRes
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.VenueFilters
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.*
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenuesViewModel(application: Application) : AndroidViewModel(application) {
    val listVenues by lazy { SingleLiveEvent<Resource<List<Any>>>() }
    val mapVenues by lazy { SingleLiveEvent<Resource<List<VenueDto>>>() }
    val joinVenue by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    private val myVenues by lazy { mutableListOf<VenueDto>() }
    private val nearbyVenues by lazy { mutableListOf<VenueDto>() }

    private var ownProfile = UserManager.getProfile()

    private var filters: VenueFilters? = null

    private var searchListQuery = ""
    private var searchMapQuery = ""

    /**
     * @return Last updated profile of logged in user.
     * */
    fun getOwnProfile() = ownProfile

    /**
     * @return Updated own profile. Should be called once after profile is updated.
     * */
    fun updatedOwnProfile(): ProfileDto {
        ownProfile = UserManager.getProfile()
        return ownProfile
    }

    fun getListVenues(showLoading: Boolean = true) {
        if (showLoading) {
            listVenues.value = Resource.loading()
        }

        val filter = this.filters
        if (filter == null) {
            RetrofitClient.conversifyApi
                    .getVenues(latitude = UserManager.getLastLatitude(),
                            longitude = UserManager.getLastLongitude())
                    .enqueue(object : Callback<ApiResponse<GetVenuesResponse>> {
                        override fun onResponse(call: Call<ApiResponse<GetVenuesResponse>>,
                                                response: Response<ApiResponse<GetVenuesResponse>>) {
                            if (response.isSuccessful) {
                                val myVenues = response.body()?.data?.myVenues ?: emptyList()
                                val nearbyVenues = response.body()?.data?.nearbyVenues
                                        ?: emptyList()

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
        } else {
            val request = getVenuesWithFilterRequest(filter)
            RetrofitClient.conversifyApi
                    .getVenuesWithFilter(request)
                    .enqueue(object : Callback<ApiResponse<List<VenueDto>>> {
                        override fun onResponse(call: Call<ApiResponse<List<VenueDto>>>,
                                                response: Response<ApiResponse<List<VenueDto>>>) {
                            if (response.isSuccessful) {
                                val nearbyVenues = response.body()?.data ?: emptyList()

                                this@VenuesViewModel.myVenues.clear()

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

                        override fun onFailure(call: Call<ApiResponse<List<VenueDto>>>, t: Throwable) {
                            listVenues.value = Resource.error(t.failureAppError())
                        }
                    })
        }
    }

    private fun getVenuesWithFilterRequest(filter: VenueFilters): GetVenuesWithFilterRequest {
        val date = DateTimeUtils.formatVenueFiltersDateForServer(filter.date?.dateTimeMillisUtc)
        val privacy = when {
            filter.privacy?.publicSelected == true && filter.privacy?.privateSelected == true ->
                listOf(ApiConstants.PRIVACY_PUBLIC, ApiConstants.PRIVACY_PRIVATE)

            filter.privacy?.publicSelected == true -> listOf(ApiConstants.PRIVACY_PUBLIC)

            filter.privacy?.privateSelected == true -> listOf(ApiConstants.PRIVACY_PRIVATE)

            else -> null
        }

        return GetVenuesWithFilterRequest(
                categoryIds = filter.categories?.filter { it.selected }?.mapNotNull { it.id },
                date = date,
                privacy = privacy,
                latitude = filter.location?.latitude,
                longitude = filter.location?.longitude)
    }

    fun getMapVenues() {
        mapVenues.value = Resource.loading()

        val filter = this.filters
        if (filter == null) {
            RetrofitClient.conversifyApi
                    .getVenues(latitude = UserManager.getLastLatitude(),
                            longitude = UserManager.getLastLongitude())
                    .enqueue(object : Callback<ApiResponse<GetVenuesResponse>> {
                        override fun onResponse(call: Call<ApiResponse<GetVenuesResponse>>,
                                                response: Response<ApiResponse<GetVenuesResponse>>) {
                            if (response.isSuccessful) {
                                val myVenues = response.body()?.data?.myVenues ?: emptyList()
                                val nearbyVenues = response.body()?.data?.nearbyVenues
                                        ?: emptyList()

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
        } else {
            val request = getVenuesWithFilterRequest(filter)
            RetrofitClient.conversifyApi
                    .getVenuesWithFilter(request)
                    .enqueue(object : Callback<ApiResponse<List<VenueDto>>> {
                        override fun onResponse(call: Call<ApiResponse<List<VenueDto>>>,
                                                response: Response<ApiResponse<List<VenueDto>>>) {
                            if (response.isSuccessful) {
                                val nearbyVenues = response.body()?.data ?: emptyList()

                                this@VenuesViewModel.myVenues.clear()

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

                        override fun onFailure(call: Call<ApiResponse<List<VenueDto>>>, t: Throwable) {
                            mapVenues.value = Resource.error(t.failureAppError())
                        }
                    })
        }
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
                            if (venue.isPrivate == true) {
                                // If venue is private then set request status to pending
                                venue.requestStatus = ApiConstants.REQUEST_STATUS_PENDING
                            } else {
                                // If venue is public then set member flag to true and set role to member
                                venue.isMember = true
                                venue.participationRole = ApiConstants.PARTICIPATION_ROLE_MEMBER
                            }
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

    fun updateFilters(filters: VenueFilters?) {
        this.filters = filters
    }

    fun getFilters(): VenueFilters? = filters

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

    private fun getAppliedFilterLabel(): String? {
        return when {
            filters?.categories != null -> {
                filters?.categories?.filter { it.selected }?.mapNotNull { it.name }?.joinToString()
            }

            filters?.date?.dateTimeMillisUtc != null -> {
                return DateTimeUtils.formatVenueFiltersDate(filters?.date?.dateTimeMillisUtc)
            }

            filters?.privacy != null -> {
                if (filters?.privacy?.privateSelected == true) {
                    getString(R.string.venue_filters_btn_private)
                } else {
                    getString(R.string.venue_filters_btn_public)
                }
            }

            filters?.location != null -> filters?.location?.name

            else -> null
        }
    }

    private fun getString(@StringRes resId: Int): String {
        return getApplication<Application>().resources.getString(resId)
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