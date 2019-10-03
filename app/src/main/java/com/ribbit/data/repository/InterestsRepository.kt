package com.ribbit.data.repository

import com.ribbit.data.MemoryCache
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.loginsignup.InterestDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InterestsRepository(private val memoryCache: MemoryCache) {
    companion object {
        private var INSTANCE: InterestsRepository? = null

        fun getInstance() = INSTANCE ?: synchronized(InterestsRepository::class.java) {
            INSTANCE ?: InterestsRepository(MemoryCache)
                    .also { INSTANCE = it }
        }
    }

    fun hasCachedInterests(): Boolean = memoryCache.hasInterests()

    fun getCachedInterests(): List<InterestDto> = memoryCache.getInterests()

    fun getInterests(callback: GetInterestsCallback? = null) {
        if (hasCachedInterests()) {
            callback?.onGetInterestsSuccess(memoryCache.getInterests())
            return
        }

        callback?.onGetInterestsLoading()

        RetrofitClient.ribbitApi
                .getInterests()
                .enqueue(object : Callback<ApiResponse<List<InterestDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<InterestDto>>>,
                                            response: Response<ApiResponse<List<InterestDto>>>) {
                        if (response.isSuccessful) {
                            val allInterests = response.body()?.data ?: emptyList()
                            callback?.onGetInterestsSuccess(allInterests)
                            memoryCache.updateInterests(allInterests)
                        } else {
                            callback?.onGetInterestsFailed(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<InterestDto>>>, t: Throwable) {
                        callback?.onGetInterestsFailed(t.failureAppError())
                    }
                })
    }

    interface GetInterestsCallback {
        fun onGetInterestsLoading()
        fun onGetInterestsSuccess(allInterests: List<InterestDto>)
        fun onGetInterestsFailed(error: AppError)
    }
}