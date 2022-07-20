package com.ribbit.ui.venues.details

import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenueDetailsViewModel : ViewModel() {
    val changeVenueNotifications by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val exitVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val archiveVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val venueDetails by lazy { SingleLiveEvent<Resource<VenueDto>>() }
    val inviteUsers by lazy { SingleLiveEvent<Resource<Any>>() }
    val editVenueName by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    fun changeVenueNotifications(venueId: String, isEnabled: Boolean) {
        changeVenueNotifications.value = Resource.loading()

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
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

    fun inviteUsersApi(emails: String, phoneNumbers: String, venueId: String) {
        inviteUsers.value = Resource.loading()

        RetrofitClient.ribbitApi
                .groupInviteUsers(emails, phoneNumbers, venueId = venueId)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            inviteUsers.value = Resource.success()
                        } else {
                            inviteUsers.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        inviteUsers.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun editVenueName(title: String, id: String) {
        RetrofitClient.ribbitApi
                .editVenueName(id, title)
                .enqueue(object : Callback<ApiResponse<VenueDto>> {
                    override fun onResponse(call: Call<ApiResponse<VenueDto>>,
                                            response: Response<ApiResponse<VenueDto>>) {
                        if (response.isSuccessful) {
                            editVenueName.value = Resource.success(response.body()?.data)
                        } else {
                            editVenueName.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<VenueDto>>, t: Throwable) {
                        editVenueName.value = Resource.error(t.failureAppError())
                    }
                })
    }
}