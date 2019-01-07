package com.conversify.ui.venues.addparticipants

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.AddVenueParticipantsRequest
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddVenueParticipantsViewModel : ViewModel() {
    val getVenueAddParticipants by lazy { MutableLiveData<Resource<List<ProfileDto>>>() }
    val addVenueParticipants by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getVenueAddParticipants(venueId: String) {
        getVenueAddParticipants.value = Resource.loading()
        RetrofitClient.conversifyApi
                .getVenueAddParticipants(venueId)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                            response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            getVenueAddParticipants.value = Resource.success(response.body()?.data)
                        } else {
                            getVenueAddParticipants.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        getVenueAddParticipants.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun addVenueParticipants(venueId: String, participantIds: List<String>) {
        addVenueParticipants.value = Resource.loading()
        val request = AddVenueParticipantsRequest(venueId, participantIds)
        RetrofitClient.conversifyApi
                .addVenueParticipants(request)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            addVenueParticipants.value = Resource.success()
                        } else {
                            addVenueParticipants.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        addVenueParticipants.value = Resource.error(t.failureAppError())
                    }
                })
    }
}