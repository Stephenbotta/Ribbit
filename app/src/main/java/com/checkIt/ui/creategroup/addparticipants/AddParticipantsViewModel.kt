package com.checkIt.ui.creategroup.addparticipants

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.loginsignup.ProfileDto
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