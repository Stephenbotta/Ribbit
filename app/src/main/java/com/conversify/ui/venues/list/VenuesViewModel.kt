package com.conversify.ui.venues.list

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VenuesViewModel : ViewModel() {
    val venues = MutableLiveData<Resource<List<Any>>>()

    private val myVenues = mutableListOf<VenueDto>()
    private val nearbyVenues = mutableListOf<VenueDto>()

    fun getVenues() {
        venues.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getVenues(latitude = UserManager.getLastLatitude(),
                        longitude = UserManager.getLastLongitude())
                .enqueue(object : Callback<ApiResponse<GetVenuesResponse>> {
                    override fun onResponse(call: Call<ApiResponse<GetVenuesResponse>>,
                                            response: Response<ApiResponse<GetVenuesResponse>>) {
                        if (response.isSuccessful) {
                            val myVenues = response.body()?.data?.myVenues ?: emptyList()
                            val nearbyVenues = response.body()?.data?.nearbyVenues ?: emptyList()

                            // Set my venue flag to true for all my venues
                            myVenues.forEach { it.myVenue = true }

                            this@VenuesViewModel.myVenues.clear()
                            this@VenuesViewModel.myVenues.addAll(myVenues)

                            this@VenuesViewModel.nearbyVenues.clear()
                            this@VenuesViewModel.nearbyVenues.addAll(nearbyVenues)

                            val venueItems = mutableListOf<Any>()
                            if (myVenues.isNotEmpty()) {
                                venueItems.add(YourVenuesDto)
                                venueItems.addAll(myVenues)
                            }

                            if (nearbyVenues.isNotEmpty()) {
                                venueItems.add(VenuesNearYouDto())
                                venueItems.addAll(nearbyVenues)
                            }

                            venues.value = Resource.success(venueItems)
                        } else {
                            venues.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetVenuesResponse>>, t: Throwable) {
                        venues.value = Resource.error(t.failureAppError())
                    }
                })
    }
}