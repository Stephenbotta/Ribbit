package com.conversify.ui.profile.settings

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav
 */
class SettingsViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val alert by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val logout by lazy { SingleLiveEvent<Resource<Any>>() }

    fun logout() {
        logout.value = Resource.loading()
        RetrofitClient.conversifyApi
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

        RetrofitClient.conversifyApi
                .getAlertNotification(action)
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