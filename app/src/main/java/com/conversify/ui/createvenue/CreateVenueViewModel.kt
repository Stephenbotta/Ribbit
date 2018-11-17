package com.conversify.ui.createvenue

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.MemoryCache
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateVenueViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }

    fun hasInterests(): Boolean = MemoryCache.hasInterests()

    fun getInterests() {
        if (hasInterests()) {
            interests.value = Resource.success(MemoryCache.getInterests())
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
}