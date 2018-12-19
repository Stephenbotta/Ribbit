package com.conversify.ui.main.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GroupPostDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class HomeViewModel : ViewModel() {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val homeFeed by lazy { MutableLiveData<Resource<PagingResult<List<GroupPostDto>>>>() }

    private var page = 1
    private var isGetHomeFeedLoading = false
    private var isLastHomeFeedReceived = false

    private var getHomeFeedCall: Call<ApiResponse<List<GroupPostDto>>>? = null

    fun validForPaging(): Boolean = !isGetHomeFeedLoading && !isLastHomeFeedReceived

    fun getHomeFeed(firstPage: Boolean = true) {
        isGetHomeFeedLoading = true
        homeFeed.value = Resource.loading()

        getHomeFeedCall?.cancel()
        val call = RetrofitClient.conversifyApi
                .getHomeFeed(page = if (firstPage) 1 else page, limit = PAGE_LIMIT)
        call.enqueue(object : Callback<ApiResponse<List<GroupPostDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<GroupPostDto>>>,
                                    response: Response<ApiResponse<List<GroupPostDto>>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastHomeFeedReceived = false
                        page = 1
                    }

                    val receivedPosts = response.body()?.data ?: emptyList()
                    if (receivedPosts.size < PAGE_LIMIT) {
                        Timber.i("Last home feed post is received")
                        isLastHomeFeedReceived = true
                    } else {
                        Timber.i("Next page for home feed posts is available")
                        ++page
                    }
                    homeFeed.value = Resource.success(PagingResult(firstPage, receivedPosts))
                } else {
                    homeFeed.value = Resource.error(response.getAppError())
                }
                isGetHomeFeedLoading = false
            }

            override fun onFailure(call: Call<ApiResponse<List<GroupPostDto>>>, t: Throwable) {
                isGetHomeFeedLoading = false
                if (!call.isCanceled) {
                    homeFeed.value = Resource.error(t.failureAppError())
                }
            }
        })
    }
}