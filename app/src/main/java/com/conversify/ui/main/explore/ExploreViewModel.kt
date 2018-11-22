package com.conversify.ui.main.explore

import android.arch.lifecycle.ViewModel
import com.conversify.data.MemoryCache
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.loginsignup.InterestDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class ExploreViewModel : ViewModel() {
    fun getInterests() {
        RetrofitClient.conversifyApi
                .getInterests()
                .enqueue(object : Callback<ApiResponse<List<InterestDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<InterestDto>>>,
                                            response: Response<ApiResponse<List<InterestDto>>>) {
                        if (response.isSuccessful) {
                            val allInterests = response.body()?.data ?: emptyList()
                            MemoryCache.updateInterests(allInterests)
                        } else {
                            Timber.w(response.getAppError().toString())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<InterestDto>>>, t: Throwable) {
                        Timber.w(t)
                    }
                })
    }
}