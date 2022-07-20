package com.ribbit.ui.profile.settings

import android.app.Application
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val alert by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val logout by lazy { SingleLiveEvent<Resource<Any>>() }

    fun logout() {
        logout.value = Resource.loading()
        RetrofitClient.ribbitApi
                .logout()
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            logout.value = Resource.success()
                        } else {
                            logout.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        logout.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun alertNotification(action: Boolean) {
        alert.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getAlertNotification(action, ApiConstants.FLAG_ALERT_NOTIFICATION)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            if (data != null) {
                                UserManager.saveProfile(data)
                            }
                            updateProfile()
                            alert.value = Resource.success(data)
                        } else {
                            alert.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        alert.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

    private fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }
}