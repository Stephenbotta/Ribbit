package com.ribbit.ui.profile.settings.blockusers

import android.app.Application
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav
 */
class BlockUsersListViewModel(application: Application) : BaseViewModel(application) {

    val blockUsersList by lazy { SingleLiveEvent<Resource<List<ProfileDto>>>() }

    fun getBlockedUsers() {
        blockUsersList.value = Resource.loading()

        RetrofitClient.ribbitApi
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

}