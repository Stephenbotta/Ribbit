package com.conversify.ui.profile.settings.hideinfo

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.data.remote.ApiConstants
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
class HidePersonalInfoViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val privateAccount by lazy { SingleLiveEvent<Resource<ProfileDto>>() }


    fun privateAccount(action: Boolean) {
        privateAccount.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getAlertNotification(action, ApiConstants.FLAG_PRIVATE_ACCOUNT)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            if (data != null) {
                                UserManager.saveProfile(data)
                            }
                            updateProfile()
                            privateAccount.value = Resource.success(data)
                        } else {
                            privateAccount.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        privateAccount.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

    private fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

}