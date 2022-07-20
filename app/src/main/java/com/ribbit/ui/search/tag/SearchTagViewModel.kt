package com.ribbit.ui.search.tag

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.PagingResult
import com.ribbit.data.remote.models.Resource
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SearchTagViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val tagSearch by lazy { MutableLiveData<Resource<PagingResult<List<ProfileDto>>>>() }
    val followUnFollow by lazy { SingleLiveEvent<Resource<Any>>() }

    private var page = 1
    private var isGetTagsLoading = false
    private var isLastTagReceived = false

    fun validForPaging(): Boolean = !isGetTagsLoading && !isLastTagReceived

    fun getTagSearch(firstPage: Boolean, search: String) {
        isGetTagsLoading = true
        tagSearch.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (firstPage)
            hashMap["pageNo"] = 1.toString()
        else
            hashMap["pageNo"] = page.toString()

        if (!search.isNullOrEmpty()) {
            hashMap["search"] = search
        }
        RetrofitClient.ribbitApi
                .getTagSearch(hashMap)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                            response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            if (firstPage) {
                                // Reset for first page
                                isLastTagReceived = false
                                page = 1
                            }

                            val receivedGroups = response.body()?.data ?: emptyList()
                            if (receivedGroups.size < PAGE_LIMIT) {
                                Timber.i("Last group is received")
                                isLastTagReceived = true
                            } else {
                                Timber.i("Next page for topic groups is available")
                                ++page
                            }

                            tagSearch.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            tagSearch.value = Resource.error(response.getAppError())
                        }
                        isGetTagsLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        isGetTagsLoading = false
                        tagSearch.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun postFollowUnFollowTag(tagId: String, action: Boolean) {
        followUnFollow.value = Resource.loading()

        RetrofitClient.ribbitApi
                .postFollowUnFollowTag(tagId, action)
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

}