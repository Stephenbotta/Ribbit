package com.ribbit.ui.groups.listing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.groups.GetGroupsResponse
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.SuggestedGroupsDto
import com.ribbit.data.remote.models.groups.YourGroupsLabelDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsViewModel : ViewModel() {
    val groups by lazy { MutableLiveData<Resource<List<Any>>>() }
    val joinGroup by lazy { SingleLiveEvent<Resource<GroupDto>>() }

    private val suggestedGroups by lazy { mutableListOf<GroupDto>() }
    private val yourGroups by lazy { mutableListOf<GroupDto>() }
    private var searchQuery = ""
    private var ownProfile = UserManager.getProfile()

    /**
     * @return Last updated profile of logged in user.
     * */
    fun getOwnProfile() = ownProfile

    /**
     * @return Updated own profile. Should be called once after profile is updated.
     * */
    fun updatedOwnProfile(): ProfileDto {
        ownProfile = UserManager.getProfile()
        return ownProfile
    }

    fun getGroups(showLoading: Boolean = true) {
        if (showLoading) {
            groups.value = Resource.loading()
        }

        RetrofitClient.ribbitApi
                .getGroups()
                .enqueue(object : Callback<ApiResponse<GetGroupsResponse>> {
                    override fun onResponse(call: Call<ApiResponse<GetGroupsResponse>>,
                                            response: Response<ApiResponse<GetGroupsResponse>>) {
                        if (response.isSuccessful) {
                            val suggestedGroups = response.body()?.data?.suggestedGroups
                                    ?: emptyList()
                            val yourGroups = response.body()?.data?.yourGroups ?: emptyList()

                            // Update group count in user's profile
                            val profile = UserManager.getProfile()
                            profile.groupCount = yourGroups.size
                            UserManager.saveProfile(profile)

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
            groupItems.add(SuggestedGroupsDto(suggestedGroups.toMutableList()))
        }

        if (yourGroups.isNotEmpty()) {
            groupItems.add(YourGroupsLabelDto)
            groupItems.addAll(yourGroups)
        }

        return groupItems
    }

    fun joinGroup(group: GroupDto) {
        joinGroup.value = Resource.loading()

        RetrofitClient.ribbitApi
                .joinGroup(groupId = group.id ?: "",
                        adminId = group.adminId ?: "",
                        isPrivate = group.isPrivate ?: false)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            if (group.isPrivate == true) {
                                // If group is private then set request status to pending
                                group.requestStatus = ApiConstants.REQUEST_STATUS_PENDING
                            } else {
                                // If group is public then set member flag to true,
                                // set role to member and increment member count.
                                group.isMember = true
                                group.participationRole = ApiConstants.PARTICIPATION_ROLE_MEMBER
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