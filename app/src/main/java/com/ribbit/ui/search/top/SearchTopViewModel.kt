package com.ribbit.ui.search.top

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.PagingResult
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Created by Manish Bhargav
 */
class SearchTopViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val topSearch by lazy { MutableLiveData<Resource<PagingResult<List<ProfileDto>>>>() }

    private var page = 1
    private var isGetTopLoading = false
    private var isLastTopReceived = false

    fun validForPaging(): Boolean = !isGetTopLoading && !isLastTopReceived

    fun getTopSearch(firstPage: Boolean, search: String) {
        isGetTopLoading = true
        topSearch.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (firstPage)
            hashMap["pageNo"] = 1.toString()
        else
            hashMap["pageNo"] = page.toString()

        if (!search.isNullOrEmpty()) {
            hashMap["search"] = search
        }
        RetrofitClient.ribbitApi
                .getTopSearch(hashMap)
                .enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>,
                                            response: Response<ApiResponse<List<ProfileDto>>>) {
                        if (response.isSuccessful) {
                            if (firstPage) {
                                // Reset for first page
                                isLastTopReceived = false
                                page = 1
                            }

                            val receivedGroups = response.body()?.data ?: emptyList()
                            if (receivedGroups.size < PAGE_LIMIT) {
                                Timber.i("Last group is received")
                                isLastTopReceived = true
                            } else {
                                Timber.i("Next page for topic groups is available")
                                ++page
                            }

                            topSearch.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            topSearch.value = Resource.error(response.getAppError())
                        }
                        isGetTopLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                        isGetTopLoading = false
                        topSearch.value = Resource.error(t.failureAppError())
                    }
                })
    }
}