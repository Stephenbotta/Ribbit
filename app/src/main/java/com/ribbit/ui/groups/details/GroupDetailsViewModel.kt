package com.ribbit.ui.groups.details

import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupDetailsViewModel : ViewModel() {

    val groupDetails by lazy { SingleLiveEvent<Resource<GroupDto>>() }
    val changeVenueNotifications by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val exitGroup by lazy { SingleLiveEvent<Resource<Any>>() }
    val archiveVenue by lazy { SingleLiveEvent<Resource<Any>>() }
    val inviteUsers by lazy { SingleLiveEvent<Resource<Any>>() }
    val editGroupName by lazy { SingleLiveEvent<Resource<Any>>() }
    fun getGroupDetails(groupId: String) {
        groupDetails.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getGroupDetails(groupId)
                .enqueue(object : Callback<ApiResponse<GroupDto>> {
                    override fun onResponse(call: Call<ApiResponse<GroupDto>>, response: Response<ApiResponse<GroupDto>>) {
                        if (response.isSuccessful) {
                            groupDetails.value = Resource.success(response.body()?.data)
                        } else {
                            groupDetails.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GroupDto>>, t: Throwable) {
                        groupDetails.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun changeVenueNotifications(groupId: String, isEnabled: Boolean) {
        changeVenueNotifications.value = Resource.loading()

        RetrofitClient.ribbitApi
                .changeGroupNotifications(groupId, isEnabled)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            changeVenueNotifications.value = Resource.success(isEnabled)
                        } else {
                            changeVenueNotifications.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        changeVenueNotifications.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun exitGroup(groupId: String) {
        exitGroup.value = Resource.loading()

        RetrofitClient.ribbitApi
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

    fun archiveVenue(groupId: String) {
        archiveVenue.value = Resource.loading()

        RetrofitClient.ribbitApi
                .archiveVenue(groupId, ApiConstants.TYPE_GROUP)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            archiveVenue.value = Resource.success()
                        } else {
                            archiveVenue.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        archiveVenue.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun inviteUsersApi(emails: String, phoneNumbers: String, groupId: String) {
        inviteUsers.value = Resource.loading()

        RetrofitClient.ribbitApi
                .groupInviteUsers(emails, phoneNumbers, groupId = groupId)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            inviteUsers.value = Resource.success()
                        } else {
                            inviteUsers.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        inviteUsers.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun editGroupName(title: String, id: String) {
        RetrofitClient.ribbitApi
                .editGroupName(id, title)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>,
                                            response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            editGroupName.value = Resource.success(response.body()?.data)
                        } else {
                            editGroupName.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        editGroupName.value = Resource.error(t.failureAppError())
                    }
                })
    }
}