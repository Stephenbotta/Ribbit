package com.ribbit.ui.profile.settings.hideinfo

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

/**
 * Created by Manish Bhargav
 */
class HidePersonalInfoViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val privateAccount by lazy { SingleLiveEvent<Resource<ProfileDto>>() }

    fun privateAccount(action: Boolean) {
        privateAccount.value = Resource.loading()

        RetrofitClient.ribbitApi
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

    fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

}