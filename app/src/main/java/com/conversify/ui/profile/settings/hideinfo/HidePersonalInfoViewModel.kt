package com.conversify.ui.profile.settings.hideinfo

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
class HidePersonalInfoViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    val blockUsersList by lazy { SingleLiveEvent<Resource<List<ProfileDto>>>() }

    fun getBlockedUsers() {
        blockUsersList.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getBlockedUsersList()
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val list = response.body()?.data ?: emptyList()
                            blockUsersList.value = Resource.success(list)
                        } else {
                            blockUsersList.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        blockUsersList.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

    private fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

}