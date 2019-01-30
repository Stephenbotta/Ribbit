package com.conversify.ui.groups.groupposts

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GetGroupPostsResponse
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class GroupPostsViewModel : ViewModel() {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val posts by lazy { MutableLiveData<Resource<PagingResult<List<GroupPostDto>>>>() }
    val exitGroup by lazy { SingleLiveEvent<Resource<Any>>() }

    private lateinit var groupId: String
    private var page = 1
    private var isGetGroupPostsLoading = false
    private var isLastGroupPostReceived = false

    private var getGroupPostsCall: Call<ApiResponse<GetGroupPostsResponse>>? = null

    fun start(group: GroupDto) {
        groupId = group.id ?: ""
    }

    fun validForPaging(): Boolean = !isGetGroupPostsLoading && !isLastGroupPostReceived

    fun getGroupPosts(firstPage: Boolean = true) {
        isGetGroupPostsLoading = true
        posts.value = Resource.loading()

        val call = RetrofitClient.conversifyApi
                .getGroupPosts(groupId, if (firstPage) 1 else page, PAGE_LIMIT)

        // Cancel any on-going call
        getGroupPostsCall?.cancel()
        getGroupPostsCall = call

        call.enqueue(object : Callback<ApiResponse<GetGroupPostsResponse>> {
            override fun onResponse(call: Call<ApiResponse<GetGroupPostsResponse>>,
                                    response: Response<ApiResponse<GetGroupPostsResponse>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastGroupPostReceived = false
                        page = 1
                    }

                    val receivedPosts = response.body()?.data?.posts ?: emptyList()
                    if (receivedPosts.size < PAGE_LIMIT) {
                        Timber.i("Last group post is received")
                        isLastGroupPostReceived = true
                    } else {
                        Timber.i("Next page for group posts is available")
                        ++page
                    }

                    posts.value = Resource.success(PagingResult(firstPage, receivedPosts))
                } else {
                    posts.value = Resource.error(response.getAppError())
                }
                isGetGroupPostsLoading = false
            }

            override fun onFailure(call: Call<ApiResponse<GetGroupPostsResponse>>, t: Throwable) {
                isGetGroupPostsLoading = false
                if (!call.isCanceled) {
                    posts.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun exitGroup() {
        exitGroup.value = Resource.loading()

        RetrofitClient.conversifyApi
                .exitGroup(groupId)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            exitGroup.value = Resource.success()
                        } else {
                            exitGroup.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        exitGroup.value = Resource.error(t.failureAppError())
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        getGroupPostsCall?.cancel()
    }
}