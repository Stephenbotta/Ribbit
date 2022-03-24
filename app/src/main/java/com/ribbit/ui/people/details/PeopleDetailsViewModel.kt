package com.ribbit.ui.people.details

import android.app.Application
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.AppConstants
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PeopleDetailsViewModel(application: Application) : BaseViewModel(application) {

    val peopleDetails by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val followUnFollow by lazy { SingleLiveEvent<Resource<Any>>() }
    val block by lazy { SingleLiveEvent<Resource<Any>>() }

    private lateinit var profile: ProfileDto

    fun getOtherUserProfileDetails(userId: String, flag: Int) {
        peopleDetails.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (flag == AppConstants.REQ_CODE_REPLY_TAG_USER) {
            hashMap["userName"] = userId
        } else {
            hashMap["userId"] = userId
        }
        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
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

        RetrofitClient.ribbitApi
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

    fun start(profile: ProfileDto) {
        this.profile = profile
    }

}