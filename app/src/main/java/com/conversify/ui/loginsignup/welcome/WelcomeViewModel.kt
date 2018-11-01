package com.conversify.ui.loginsignup.welcome

import android.arch.lifecycle.ViewModel
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.loginsignup.SignUpRequest
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WelcomeViewModel : ViewModel() {
    val signUp by lazy { SingleLiveEvent<Resource<Any>>() }

    fun signUp(request: SignUpRequest) {
        signUp.value = Resource.loading()

        RetrofitClient.conversifyApi
                .signUp(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            signUp.value = Resource.success()
                        } else {
                            signUp.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        signUp.value = Resource.error(t.failureAppError())
                    }
                })
    }
}