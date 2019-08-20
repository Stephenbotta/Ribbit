package com.checkIt.ui.loginsignup.loginpassword

import androidx.lifecycle.ViewModel
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.loginsignup.LoginRequest
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPasswordViewModel : ViewModel() {
    val login by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val resetPassword by lazy { SingleLiveEvent<Resource<Any>>() }

    fun login(request: LoginRequest) {
        login.value = Resource.loading()

        RetrofitClient.conversifyApi
                .login(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            login.value = Resource.success(profile)
                        } else {
                            login.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        login.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun resetPassword(email: String) {
        resetPassword.value = Resource.loading()

        RetrofitClient.conversifyApi
                .resetPassword(email)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            resetPassword.value = Resource.success(response.message())
                        } else {
                            resetPassword.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        resetPassword.value = Resource.error(t.failureAppError())
                    }
                })
    }
}