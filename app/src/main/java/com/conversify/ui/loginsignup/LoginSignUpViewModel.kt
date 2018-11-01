package com.conversify.ui.loginsignup

import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.LoginRequest
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginSignUpViewModel : ViewModel() {
    val loginRegister by lazy { SingleLiveEvent<Resource<ProfileDto>>() }

    fun registerEmailOrPhoneNumber(email: String? = null, countryCode: String? = null, phoneNumber: String? = null) {
        loginRegister.value = Resource.loading()

        RetrofitClient.conversifyApi
                .registerEmailOrPhoneNumber(email, countryCode, phoneNumber)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            loginRegister.value = Resource.success(response.body()?.data)
                        } else {
                            loginRegister.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        loginRegister.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun login(request: LoginRequest) {
        loginRegister.value = Resource.loading()

        RetrofitClient.conversifyApi
                .login(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            loginRegister.value = Resource.success(response.body()?.data)
                        } else {
                            loginRegister.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        loginRegister.value = Resource.error(t.failureAppError())
                    }
                })
    }
}