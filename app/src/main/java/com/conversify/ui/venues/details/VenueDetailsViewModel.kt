package com.conversify.ui.venues.details

import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.Resource
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenueDetailsViewModel : ViewModel() {
    val changeVenueNotifications by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val exitVenue by lazy { SingleLiveEvent<Resource<Any>>() }

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
}