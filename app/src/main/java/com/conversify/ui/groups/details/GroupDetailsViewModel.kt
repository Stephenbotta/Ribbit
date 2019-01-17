package com.conversify.ui.groups.details

import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupDetailsViewModel : ViewModel() {

    val groupDetails by lazy { SingleLiveEvent<Resource<GroupDto>>() }
    val changeVenueNotifications by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val exitVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val archiveVenue by lazy { SingleLiveEvent<Resource<Any>>() }


    fun getGroupDetails(groupId: String) {
        groupDetails.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getGroupDetails(groupId)
                .enqueue(object : Callback<ApiResponse<GroupDto>> {
                    override fun onResponse(call: Call<ApiResponse<GroupDto>>, response: Response<ApiResponse<GroupDto>>) {
                        if (response.isSuccessful) {
                            groupDetails.value = Resource.success(response.body()?.data)
                        } else {
                            groupDetails.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GroupDto>>, t: Throwable) {
                        groupDetails.value = Resource.error(t.failureAppError())
                    }
                })
    }


    fun changeVenueNotifications(groupId: String, isEnabled: Boolean) {
        changeVenueNotifications.value = Resource.loading()

        RetrofitClient.conversifyApi
                .changeVenueNotifications(groupId, isEnabled)
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
                .archiveVenue(venueId)
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
}