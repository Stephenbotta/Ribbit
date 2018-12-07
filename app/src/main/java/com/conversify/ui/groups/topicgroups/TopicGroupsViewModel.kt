package com.conversify.ui.groups.topicgroups

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.InterestDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class TopicGroupsViewModel : ViewModel() {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val groups by lazy { MutableLiveData<Resource<PagingResult<List<GroupDto>>>>() }

    private lateinit var topicId: String
    private var page = 1
    private var isGetGroupsLoading = false
    private var isLastGroupReceived = false

    fun start(topic: InterestDto) {
        topicId = topic.id ?: ""
    }

    fun validForPaging(): Boolean = !isGetGroupsLoading && !isLastGroupReceived

    fun getGroups(firstPage: Boolean = true) {
        isGetGroupsLoading = true
        groups.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getTopicGroups(topicId, if (firstPage) 1 else page, PAGE_LIMIT)
                .enqueue(object : Callback<ApiResponse<List<GroupDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<GroupDto>>>,
                                            response: Response<ApiResponse<List<GroupDto>>>) {
                        if (response.isSuccessful) {
                            if (firstPage) {
                                // Reset for first page
                                isLastGroupReceived = false
                                page = 1
                            }

                            val receivedGroups = response.body()?.data ?: emptyList()
                            if (receivedGroups.size < PAGE_LIMIT) {
                                Timber.i("Last group is received")
                                isLastGroupReceived = true
                            } else {
                                Timber.i("Next page for topic groups is available")
                                ++page
                            }

                            groups.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            groups.value = Resource.error(response.getAppError())
                        }
                        isGetGroupsLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<GroupDto>>>, t: Throwable) {
                        isGetGroupsLoading = false
                        groups.value = Resource.error(t.failureAppError())
                    }
                })
    }
}