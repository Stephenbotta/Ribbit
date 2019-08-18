package com.checkIt.ui.profile.followerandfollowing

import android.app.Application
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.ui.base.BaseViewModel
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav
 */
class FollowerAndFollowingViewModel(application: Application) : BaseViewModel(application) {

    val followerList by lazy { SingleLiveEvent<Resource<List<Any>>>() }
    private val filteredList by lazy { mutableListOf<ProfileDto>() }
    private var searchQuery = ""

    fun getUsers(flag: Int) {
        followerList.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getFollowerFollowingList(flag)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@FollowerAndFollowingViewModel.filteredList.clear()
                            this@FollowerAndFollowingViewModel.filteredList.addAll(data)

                            val items = if (searchQuery.isBlank()) {
                                getItems(data)
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

        RetrofitClient.conversifyApi
                .getPostLikeList(postId)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@FollowerAndFollowingViewModel.filteredList.clear()
                            this@FollowerAndFollowingViewModel.filteredList.addAll(data)

                            val items = if (searchQuery.isBlank()) {
                                getItems(data)
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
    private fun getSearchResult(query: String): List<Any> {
        // If query is blank then return the result with all username
        if (query.isBlank())
            return getItems(filteredList)

        val yourResult = filteredList.filter {
            (it.userName ?: "").toLowerCase().contains(query.toLowerCase())
        }

        return getItems(yourResult)
    }

    /**
     * Returns items in correct order
     * */
    private fun getItems(list: List<ProfileDto>): List<Any> {
        val items = mutableListOf<Any>()
        if (list.isNotEmpty())
            items.addAll(list)
        return items
    }

}