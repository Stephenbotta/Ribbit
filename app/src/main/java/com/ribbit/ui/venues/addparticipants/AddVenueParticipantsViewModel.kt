package com.ribbit.ui.venues.addparticipants

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.groups.AddGroupParticipantsRequest
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.venues.AddVenueParticipantsRequest
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddVenueParticipantsViewModel : ViewModel() {
    val getVenueAddParticipants by lazy { MutableLiveData<Resource<List<ProfileDto>>>() }
    val addVenueParticipants by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getVenueAddParticipants(hashMap: HashMap<String, String>) {
        getVenueAddParticipants.value = Resource.loading()

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
                .addVenueParticipants(AddVenueParticipantsRequest(venueId, participantIds))
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

        RetrofitClient.ribbitApi
                .addGroupParticipants(AddGroupParticipantsRequest(groupId, participantIds))
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