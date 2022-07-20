package com.ribbit.ui.loginsignup.welcome

import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.remote.models.loginsignup.SignUpRequest
import com.ribbit.data.remote.models.loginsignup.UsernameAvailabilityResponse
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WelcomeViewModel : ViewModel() {
    val signUp by lazy { SingleLiveEvent<Resource<Any>>() }
    val usernameAvailability by lazy { SingleLiveEvent<Resource<Boolean>>() }
    private var usernameAvailable = false

    private var usernameAvailabilityCall: Call<ApiResponse<UsernameAvailabilityResponse>>? = null

    fun signUp(request: SignUpRequest) {
        signUp.value = Resource.loading()

        RetrofitClient.ribbitApi
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

    fun checkUsernameAvailability(username: String) {
        usernameAvailable = false
        usernameAvailability.value = Resource.loading()

        usernameAvailabilityCall?.cancel()
        val call = RetrofitClient.ribbitApi.usernameAvailability(username)
        usernameAvailabilityCall = call
        call.enqueue(object : Callback<ApiResponse<UsernameAvailabilityResponse>> {
            override fun onResponse(call: Call<ApiResponse<UsernameAvailabilityResponse>>,
                                    response: Response<ApiResponse<UsernameAvailabilityResponse>>) {
                if (response.isSuccessful) {
                    val isAvailable = response.body()?.data?.isAvailable ?: false
                    usernameAvailable = isAvailable
                    usernameAvailability.value = Resource.success(isAvailable)
                } else {
                    usernameAvailable = false
                    usernameAvailability.value = Resource.error(response.getAppError())
                }
            }

            override fun onFailure(call: Call<ApiResponse<UsernameAvailabilityResponse>>, t: Throwable) {
                usernameAvailable = false
                if (!call.isCanceled) {
                    usernameAvailability.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun updateUsernameAvailability(isAvailable: Boolean) {
        if (!isAvailable) {
            usernameAvailabilityCall?.cancel()
        }

        usernameAvailable = isAvailable
    }

    fun isUsernameAvailable(): Boolean = usernameAvailable

    override fun onCleared() {
        super.onCleared()
        usernameAvailabilityCall?.cancel()
    }
}