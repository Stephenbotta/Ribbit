package com.ribbit.ui.profile.followerandfollowing

import android.app.Application
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowerAndFollowingViewModel(application: Application) : BaseViewModel(application) {

    val followerList by lazy { SingleLiveEvent<Resource<List<ProfileDto>>>() }
    private val filteredList by lazy { mutableListOf<ProfileDto>() }
    private var searchQuery = ""

    fun getUsers(flag: Int) {
        followerList.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getFollowerFollowingList(flag)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@FollowerAndFollowingViewModel.filteredList.clear()
                            this@FollowerAndFollowingViewModel.filteredList.addAll(data)

                            val items = if (searchQuery.isBlank()) {
                                data
                            } else {
                                getSearchResult(searchQuery)
                            }

                            followerList.value = Resource.success(items)
                        } else {
                            followerList.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        followerList.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getLikeUserList(postId: String) {
        followerList.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getPostLikeList(postId)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@FollowerAndFollowingViewModel.filteredList.clear()
                            this@FollowerAndFollowingViewModel.filteredList.addAll(data)

                            val items = if (searchQuery.isBlank()) {
                                data
                            } else {
                                getSearchResult(searchQuery)
                            }

                            followerList.value = Resource.success(items)
                        } else {
                            followerList.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        followerList.value = Resource.error(t.failureAppError())
                    }
                })
    }


    fun searchUsername(query: String) {
        this.searchQuery = query
        val searchResult = getSearchResult(query)
        followerList.value = Resource.success(searchResult)
    }

    /**
     * Returns list items in correct order after applying the search filter
     * */
    private fun getSearchResult(query: String): List<ProfileDto> {
        // If query is blank then return the result with all username
        if (query.isBlank())
            return filteredList

        return filteredList.filter {
            (it.userName ?: "").toLowerCase().contains(query.toLowerCase())
        }
    }
}