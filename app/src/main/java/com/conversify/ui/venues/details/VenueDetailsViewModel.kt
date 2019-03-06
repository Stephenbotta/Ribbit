package com.conversify.ui.venues.details

import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenueDetailsViewModel : ViewModel() {
    val changeVenueNotifications by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val exitVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val archiveVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val venueDetails by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    fun changeVenueNotifications(venueId: String, isEnabled: Boolean) {
        changeVenueNotifications.value = Resource.loading()

        RetrofitClient.conversifyApi
                .changeVenueNotifications(venueId, isEnabled)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            changeVenueNotifications.value = Resource.success(isEnabled)
                        } else {
                            changeVenueNotifications.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        changeVenueNotifications.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun exitVenue(venueId: String) {
        exitVenue.value = Resource.loading()

        RetrofitClient.conversifyApi
                .exitVenue(venueId)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            exitVenue.value = Resource.success()
                        } else {
                            exitVenue.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        exitVenue.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun archiveVenue(venueId: String) {
        archiveVenue.value = Resource.loading()

        RetrofitClient.conversifyApi
                .archiveVenue(venueId, ApiConstants.TYPE_VENUE)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            archiveVenue.value = Resource.success()
                        } else {
                            archiveVenue.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        archiveVenue.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getVenueDetails(venueId: String) {
        venueDetails.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getVenueDetails(venueId)
                .enqueue(object : Callback<ApiResponse<VenueDto>> {
                    override fun onResponse(call: Call<ApiResponse<VenueDto>>, response: Response<ApiResponse<VenueDto>>) {
                        if (response.isSuccessful) {
                            venueDetails.value = Resource.success(response.body()?.data)
                        } else {
                            venueDetails.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<VenueDto>>, t: Throwable) {
                        venueDetails.value = Resource.error(t.failureAppError())
                    }
                })
    }

}