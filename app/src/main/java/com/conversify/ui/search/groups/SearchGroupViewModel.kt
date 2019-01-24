package com.conversify.ui.search.groups

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseViewModel
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

    private var page = 1
    private var isGetGroupLoading = false
    private var isLastGroupReceived = false

    fun validForPaging(): Boolean = !isGetGroupLoading && !isLastGroupReceived

    fun getGroupSearch(firstPage: Boolean,search:String) {
        isGetGroupLoading = true
        groupSearch.value = Resource.loading()

        val hashMap= hashMapOf<String,String>()
        if (firstPage)
            hashMap.put("pageNo",1.toString())
        else
        hashMap.put("pageNo",page.toString())

        if (!search.isNullOrEmpty()){
            hashMap.put("search",search)
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
}