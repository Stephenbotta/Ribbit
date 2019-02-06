package com.conversify.ui.people.details

import android.app.Application
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

class PeopleDetailsViewModel(application: Application) : BaseViewModel(application) {

    val peopleDetails by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val followUnFollow by lazy { SingleLiveEvent<Resource<Any>>() }
    val block by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getOtherUserProfileDetails(userId: String) {
        peopleDetails.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        hashMap.put("userId", userId)
        RetrofitClient.conversifyApi
                .getUserProfileDetails(hashMap)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            peopleDetails.value = Resource.success(response.body()?.data)
                        } else {
                            peopleDetails.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        peopleDetails.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun postFollowUnFollow(userId: String, action: Double) {
        followUnFollow.value = Resource.loading()

        RetrofitClient.conversifyApi
                .postFollowUnFollow(userId, action)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            followUnFollow.value = Resource.success(response.body()?.data)
                        } else {
                            followUnFollow.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        followUnFollow.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun postBlock(userId: String, action: Double) {
        block.value = Resource.loading()

        RetrofitClient.conversifyApi
                .postBlock(userId, action)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            block.value = Resource.success(response.body()?.data)
                        } else {
                            block.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        block.value = Resource.error(t.failureAppError())
                    }
                })
    }

}