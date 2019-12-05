package com.ribbit.ui.loginsignup

import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.LoginRequest
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.loginsignup.SignUpRequest
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginSignUpViewModel : ViewModel() {

    val loginRegister by lazy { SingleLiveEvent<Resource<ProfileDto>>() }

    fun registerEmailOrPhoneNumber(email: String? = null, countryCode: String? = null, phoneNumber: String? = null) {
        loginRegister.value = Resource.loading()

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
                .login(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null && profile.isProfileComplete == true &&
                                    !profile.accessToken.isNullOrBlank()) {
                                UserManager.saveProfile(profile)
                            }
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

    fun signUp(request: SignUpRequest) {
        loginRegister.value = Resource.loading()

        RetrofitClient.ribbitApi
                .signUp(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null && profile.isProfileComplete == true) {
                                UserManager.saveProfile(profile)
                            }
                            loginRegister.value = Resource.success(profile)
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