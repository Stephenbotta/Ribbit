package com.checkIt.ui.search.groups

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.PagingResult
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.ui.base.BaseViewModel
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Manish Bhargav
 */
class SearchGroupViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val groupSearch by lazy { MutableLiveData<Resource<PagingResult<List<GroupDto>>>>() }
    val joinGroup by lazy { SingleLiveEvent<Resource<GroupDto>>() }

    private var page = 1
    private var isGetGroupLoading = false
    private var isLastGroupReceived = false

    fun validForPaging(): Boolean = !isGetGroupLoading && !isLastGroupReceived

    fun getGroupSearch(firstPage: Boolean, search: String) {
        isGetGroupLoading = true
        groupSearch.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (firstPage)
            hashMap["pageNo"] = 1.toString()
        else
            hashMap["pageNo"] = page.toString()

        if (!search.isNullOrEmpty()) {
            hashMap["search"] = search
        }
        RetrofitClient.conversifyApi
                .getGroupSearch(hashMap)
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

                            groupSearch.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            groupSearch.value = Resource.error(response.getAppError())
                        }
                        isGetGroupLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<GroupDto>>>, t: Throwable) {
                        isGetGroupLoading = false
                        groupSearch.value = Resource.error(t.failureAppError())
                    }
                })
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