package com.conversify.ui.loginsignup.chooseinterests

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.MemoryCache
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChooseInterestsViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }
    val updateInterests by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getInterests() {
        val cachedInterests = MemoryCache.getInterests()
        if (cachedInterests.isNotEmpty()) {
            interests.value = Resource.success(cachedInterests)
            return
        }

        interests.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getInterests()
                .enqueue(object : Callback<ApiResponse<List<InterestDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<InterestDto>>>,
                                            response: Response<ApiResponse<List<InterestDto>>>) {
                        if (response.isSuccessful) {
                            val allInterests = response.body()?.data ?: emptyList()
                            interests.value = Resource.success(allInterests)
                            MemoryCache.updateInterests(allInterests)
                        } else {
                            interests.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<InterestDto>>>, t: Throwable) {
                        interests.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun updateInterests(selectedInterests: List<InterestDto>) {
        val interests = selectedInterests.mapNotNull { it.id }

        updateInterests.value = Resource.loading()

        RetrofitClient.conversifyApi
                .updateInterests(interests)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            updateInterests.value = Resource.success()
                        } else {
                            updateInterests.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        updateInterests.value = Resource.error(t.failureAppError())
                    }
                })
    }
}