package com.checkIt.ui.profile.settings.hideinfo.hidestatus

import android.app.Application
import com.google.gson.Gson
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.ApiConstants
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

class HideStatusViewModel(application: Application) : BaseViewModel(application) {
    private val gson by lazy { Gson() }
    private var profile = UserManager.getProfile()
    val followerList by lazy { SingleLiveEvent<Resource<List<Any>>>() }
    private val filteredList by lazy { mutableListOf<ProfileDto>() }
    val configSetting by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    private var searchQuery = ""

    fun configSetting(map: HashMap<String, String>) {
        configSetting.value = Resource.loading()

        RetrofitClient.conversifyApi
                .postConfigSetting(map)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            if (data != null) {
                                UserManager.saveProfile(data)
                            }
                            updateProfile()
                            configSetting.value = Resource.success(data)
                        } else {
                            configSetting.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        configSetting.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun configSetting(flag: Int, selectedUserIds: List<String>) {
        configSetting.value = Resource.loading()

        RetrofitClient.conversifyApi
                .postConfigSettingUserArray(flag, gson.toJson(selectedUserIds))
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            if (data != null) {
                                UserManager.saveProfile(data)
                            }
                            updateProfile()
                            configSetting.value = Resource.success(data)
                        } else {
                            configSetting.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        configSetting.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getFollowerList() {
        followerList.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getFollowerFollowingList(ApiConstants.FLAG_FOLLOWERS)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()

                            this@HideStatusViewModel.filteredList.clear()
                            this@HideStatusViewModel.filteredList.addAll(data)

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

    fun getProfile() = profile

    private fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

}