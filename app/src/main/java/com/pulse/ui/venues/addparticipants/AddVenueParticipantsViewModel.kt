package com.pulse.ui.venues.addparticipants

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.pulse.data.remote.RetrofitClient
import com.pulse.data.remote.failureAppError
import com.pulse.data.remote.getAppError
import com.pulse.data.remote.models.ApiResponse
import com.pulse.data.remote.models.Resource
import com.pulse.data.remote.models.groups.AddGroupParticipantsRequest
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.data.remote.models.venues.AddVenueParticipantsRequest
import com.pulse.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddVenueParticipantsViewModel : ViewModel() {
    val getVenueAddParticipants by lazy { MutableLiveData<Resource<List<ProfileDto>>>() }
    val addVenueParticipants by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getVenueAddParticipants(hashMap: HashMap<String, String>) {
        getVenueAddParticipants.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getVenueAddParticipants(hashMap)
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

        RetrofitClient.conversifyApi
                .addVenueParticipants( AddVenueParticipantsRequest(venueId, participantIds))
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

    fun addGroupParticipants(groupId: String, participantIds: List<String>) {
        addVenueParticipants.value = Resource.loading()

        RetrofitClient.conversifyApi
                .addGroupParticipants( AddGroupParticipantsRequest(groupId, participantIds))
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