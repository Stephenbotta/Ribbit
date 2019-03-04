package com.conversify.ui.main.searchusers

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SearchUsersViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    private var profile = UserManager.getProfile()
    val matchedResults = MutableLiveData<Resource<PagingResult<List<ProfileDto>>>>()
    private var call: Call<*>? = null
    private var page = 1
    private var isLoading = false
    private var isLastItemReceived = false

    fun validForPaging(): Boolean = !isLoading && !isLastItemReceived

    fun getProfile() = profile

    fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

    fun getMatchedResultsApi(latitude: Double, longitude: Double, range: Int, isFirstPage: Boolean,
                             categoryIds: List<String>) {
        isLoading = true
        val interestCall = RetrofitClient.conversifyApi.interestMatchUsers(longitude,
                latitude, range, if (isFirstPage) 1 else page, categoryIds)
        call?.cancel()
        call = interestCall
        interestCall.enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
            override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                if (!call.isCanceled)
                    matchedResults.value = Resource.error(t.failureAppError())
            }

            override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                if (response.isSuccessful) {
                    if (isFirstPage) {
                        // Reset for first page
                        isLastItemReceived = false
                        page = 1
                    }

                    val matchedUsersList = response.body()?.data ?: emptyList()

                    if (matchedUsersList.size < PAGE_LIMIT) {
                        Timber.i("Last notification is received")
                        isLastItemReceived = true
                    } else {
                        Timber.i("Next page for notifications is available")
                        ++page
                    }

                    matchedResults.value = Resource.success(PagingResult(isFirstPage, matchedUsersList))
                } else {
                    matchedResults.value = Resource.error(response.getAppError())
                }
            }
        })
    }

    fun cancelGettingResultsFromApi() {
        call?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        call?.cancel()
    }
}