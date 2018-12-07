package com.conversify.ui.groups.listing

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GetGroupsResponse
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.SuggestedGroupsDto
import com.conversify.data.remote.models.groups.YourGroupsDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsViewModel : ViewModel() {
    val groups by lazy { MutableLiveData<Resource<List<Any>>>() }
    val joinGroup by lazy { SingleLiveEvent<Resource<GroupDto>>() }

    private val suggestedGroups by lazy { mutableListOf<GroupDto>() }
    private val yourGroups by lazy { mutableListOf<GroupDto>() }
    private var searchQuery = ""

    fun getGroups(showLoading: Boolean = true) {
        if (showLoading) {
            groups.value = Resource.loading()
        }

        RetrofitClient.conversifyApi
                .getGroups()
                .enqueue(object : Callback<ApiResponse<GetGroupsResponse>> {
                    override fun onResponse(call: Call<ApiResponse<GetGroupsResponse>>,
                                            response: Response<ApiResponse<GetGroupsResponse>>) {
                        if (response.isSuccessful) {
                            val suggestedGroups = response.body()?.data?.suggestedGroups
                                    ?: emptyList()
                            val yourGroups = response.body()?.data?.yourGroups ?: emptyList()

                            this@GroupsViewModel.yourGroups.clear()
                            this@GroupsViewModel.yourGroups.addAll(yourGroups)

                            this@GroupsViewModel.suggestedGroups.clear()
                            this@GroupsViewModel.suggestedGroups.addAll(suggestedGroups)

                            val groupItems = if (searchQuery.isBlank()) {
                                getGroupsItems(suggestedGroups, yourGroups)
                            } else {
                                getSearchGroupsResult(searchQuery)
                            }

                            groups.value = Resource.success(groupItems)
                        } else {
                            groups.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetGroupsResponse>>, t: Throwable) {
                        groups.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun searchGroups(query: String) {
        this.searchQuery = query
        val searchResult = getSearchGroupsResult(query)
        groups.value = Resource.success(searchResult)
    }

    /**
     * Returns groups list items in correct order after applying the search filter
     * */
    private fun getSearchGroupsResult(query: String): List<Any> {
        // If query is blank then return the result with all groups
        if (query.isBlank()) {
            return getGroupsItems(suggestedGroups, yourGroups)
        }

        val suggestedGroupsResult = suggestedGroups.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }
        val yourGroupsResult = yourGroups.filter {
            (it.name ?: "").toLowerCase().contains(query.toLowerCase())
        }

        return getGroupsItems(suggestedGroupsResult, yourGroupsResult)
    }

    /**
     * Returns groups items in correct order
     * */
    private fun getGroupsItems(suggestedGroups: List<GroupDto>, yourGroups: List<GroupDto>): List<Any> {
        val groupItems = mutableListOf<Any>()

        if (suggestedGroups.isNotEmpty()) {
            groupItems.add(SuggestedGroupsDto(suggestedGroups))
        }

        if (yourGroups.isNotEmpty()) {
            groupItems.add(YourGroupsDto)
            groupItems.addAll(yourGroups)
        }

        return groupItems
    }

    fun joinGroup(group: GroupDto) {
        joinGroup.value = Resource.loading()

        RetrofitClient.conversifyApi
                .joinGroup(groupId = group.id ?: "",
                        adminId = group.adminId ?: "",
                        isPrivate = group.isPrivate ?: false)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            // Set member flag to true and increment member count for public group
                            if (group.isPrivate == false) {
                                group.isMember = true
                                val updatedCount = (group.memberCount ?: 0) + 1
                                group.memberCount = updatedCount
                            }
                            joinGroup.value = Resource.success(group)
                        } else {
                            joinGroup.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        joinGroup.value = Resource.error(t.failureAppError())
                    }
                })
    }
}