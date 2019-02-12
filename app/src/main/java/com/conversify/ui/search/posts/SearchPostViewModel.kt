package com.conversify.ui.search.posts

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.ui.base.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Manish Bhargav
 */
class SearchPostViewModel(application: Application) : BaseViewModel(application) {

    companion object {
        private const val PAGE_LIMIT = 10
    }

    val postSearch by lazy { MutableLiveData<Resource<PagingResult<List<GroupPostDto>>>>() }

    private var page = 1
    private var isGetPostLoading = false
    private var isLastPostReceived = false

    fun validForPaging(): Boolean = !isGetPostLoading && !isLastPostReceived

    fun getPostSearch(firstPage: Boolean, search: String) {
        isGetPostLoading = true
        postSearch.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (firstPage)
            hashMap["pageNo"] = 1.toString()
        else
            hashMap["pageNo"] = page.toString()

        if (!search.isNullOrEmpty()) {
            hashMap["search"] = search
        }
        RetrofitClient.conversifyApi
                .getPostSearch(hashMap)
                .enqueue(object : Callback<ApiResponse<List<GroupPostDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<GroupPostDto>>>,
                                            response: Response<ApiResponse<List<GroupPostDto>>>) {
                        if (response.isSuccessful) {
                            if (firstPage) {
                                // Reset for first page
                                isLastPostReceived = false
                                page = 1
                            }

                            val receivedGroups = response.body()?.data ?: emptyList()
                            if (receivedGroups.size < PAGE_LIMIT) {
                                Timber.i("Last group is received")
                                isLastPostReceived = true
                            } else {
                                Timber.i("Next page for topic groups is available")
                                ++page
                            }

                            postSearch.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            postSearch.value = Resource.error(response.getAppError())
                        }
                        isGetPostLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<GroupPostDto>>>, t: Throwable) {
                        isGetPostLoading = false
                        postSearch.value = Resource.error(t.failureAppError())
                    }
                })
    }

}