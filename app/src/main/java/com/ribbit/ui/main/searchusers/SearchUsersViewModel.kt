package com.ribbit.ui.main.searchusers

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.PagingResult
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.SearchUser
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.ui.base.BaseViewModel
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
                             categoryIds: ArrayList<String>) {
        isLoading = true
        val request = SearchUser(locationLat = latitude, locationLong = longitude,
                range = range, pageNo = if (isFirstPage) 1 else page, categoryIds = categoryIds)
        val interestCall = RetrofitClient.ribbitApi.interestMatchUsers(request)
        call?.cancel()
        call = interestCall
        interestCall.enqueue(object : Callback<ApiResponse<List<ProfileDto>>> {
            override fun onFailure(call: Call<ApiResponse<List<ProfileDto>>>, t: Throwable) {
                if (!call.isCanceled)
                    matchedResults.value = Resource.error(t.failureAppError())
                isLoading = false
            }

            override fun onResponse(call: Call<ApiResponse<List<ProfileDto>>>, response: Response<ApiResponse<List<ProfileDto>>>) {
                isLoading = false
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