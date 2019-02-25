package com.conversify.ui.creategroup.addparticipants

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddParticipantsViewModel : ViewModel() {
    val followers by lazy { MutableLiveData<Resource<List<ProfileDto>>>() }

    fun getFollowers() {
        followers.value = Resource.loading()
        RetrofitClient.conversifyApi
                .getFollowerFollowingList(ApiConstants.FLAG_FOLLOWERS)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                            response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val receivedFollowers = response.body()?.data ?: emptyList()
                            followers.value = Resource.success(receivedFollowers)
                        } else {
                            followers.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        followers.value = Resource.error(t.failureAppError())
                    }
                })
    }
}