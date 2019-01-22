package com.conversify.ui.search.top

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Manish Bhargav on 22/1/19
 */
class SearchTopViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val groups by lazy { MutableLiveData<Resource<PagingResult<List<ProfileDto>>>>() }

    private var page = 1
    private var isGetGroupsLoading = false
    private var isLastGroupReceived = false

    fun validForPaging(): Boolean = !isGetGroupsLoading && !isLastGroupReceived

    fun getTopSearch(firstPage: Boolean = true,search:String) {
        isGetGroupsLoading = true
        groups.value = Resource.loading()

        val hashMap= hashMapOf<String,String>()
        if (firstPage)
            hashMap.put("pageNo",1.toString())
        else
        hashMap.put("pageNo",page.toString())

        if (!search.isNullOrEmpty()){
            hashMap.put("search",search)
        }
        RetrofitClient.conversifyApi
                .getTopSearch(hashMap)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                            response: Response<ApiResponse<List<ProfileDto>>>) {
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

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        isGetGroupsLoading = false
                        groups.value = Resource.error(t.failureAppError())
                    }
                })
    }
}