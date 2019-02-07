package com.conversify.ui.profile.settings

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Uploader
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.profile.CreateEditProfileRequest
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * Created by Manish Bhargav
 */
class SettingsViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val editProfile by lazy { SingleLiveEvent<Resource<ApiResponse<ProfileDto>>>() }
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

    private fun updateProfileApi(request: CreateEditProfileRequest) {
        editProfile.value = Resource.loading()

        RetrofitClient.conversifyApi
                .editProfile(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            editProfile.value = Resource.success()
                        } else {
                            editProfile.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        editProfile.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

}