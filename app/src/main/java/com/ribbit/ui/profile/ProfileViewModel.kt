package com.ribbit.ui.profile

import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private var profile = UserManager.getProfile()
    val peopleDetails by lazy { SingleLiveEvent<Resource<ProfileDto>>() }

    fun getUserProfileDetails() {
        peopleDetails.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getUserProfileDetails(HashMap())
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            profile = response.body()?.data ?: ProfileDto()
                            peopleDetails.value = Resource.success(profile)
                        } else {
                            peopleDetails.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        peopleDetails.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

}