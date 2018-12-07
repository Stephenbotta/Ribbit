package com.conversify.ui.venues

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.StringRes
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.VenueFilters
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.venues.GetVenuesResponse
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.VenuesNearYouDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.utils.AppConstants
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenuesViewModel(application: Application) : AndroidViewModel(application) {
    val listVenues by lazy { MutableLiveData<Resource<List<Any>>>() }
    val mapVenues by lazy { MutableLiveData<Resource<List<VenueDto>>>() }
    val joinVenue by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    private val myVenues by lazy { mutableListOf<VenueDto>() }
    private val nearbyVenues by lazy { mutableListOf<VenueDto>() }

    private var filters: VenueFilters? = null

    private var searchListQuery = ""
    private var searchMapQuery = ""

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
            val categoryId = filter.category?.id
            val date = DateTimeUtils.formatVenueFiltersDateForServer(filter.date?.dateTimeMillisUtc)
            val isPrivate = when {
                filter.privacy?.isPrivate == null -> null

                else -> if (filter.privacy.isPrivate == true) {
                    AppConstants.PRIVATE_TRUE
                } else {
                    AppConstants.PRIVATE_FALSE
                }
            }
            RetrofitClient.conversifyApi
                    .getVenuesWithFilter(categoryId = categoryId,
                            date = date,
                            isPrivate = isPrivate,
                            latitude = filter.location?.latitude,
                            longitude = filter.location?.longitude)
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
            val categoryId = filter.category?.id
            val date = DateTimeUtils.formatVenueFiltersDateForServer(filter.date?.dateTimeMillisUtc)
            val isPrivate = when {
                filter.privacy?.isPrivate == null -> null

                else -> if (filter.privacy.isPrivate == true) {
                    AppConstants.PRIVATE_TRUE
                } else {
                    AppConstants.PRIVATE_FALSE
                }
            }
            RetrofitClient.conversifyApi
                    .getVenuesWithFilter(categoryId = categoryId,
                            date = date,
                            isPrivate = isPrivate,
                            latitude = filter.location?.latitude,
                            longitude = filter.location?.longitude)
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
                            // If venue is public then set member flag to true
                            if (venue.isPrivate == false) {
                                venue.isMember = true
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
            venueItems.add(VenuesNearYouDto(getAppliedFilterLabel()))
            venueItems.addAll(nearbyVenues)
        }

        return venueItems
    }

    private fun getAppliedFilterLabel(): String? {
        return when {
            filters?.category?.name != null -> {
                filters?.category?.name
            }

            filters?.date?.dateTimeMillisUtc != null -> {
                return DateTimeUtils.formatVenueFiltersDate(filters?.date?.dateTimeMillisUtc)
            }

            filters?.privacy?.isPrivate != null -> {
                if (filters?.privacy?.isPrivate == true) {
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