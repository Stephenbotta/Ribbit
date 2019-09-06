package com.checkIt.ui.search.venues

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.PagingResult
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.venues.VenueDto
import com.checkIt.ui.base.BaseViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SearchVenueViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val venueSearch by lazy { MutableLiveData<Resource<PagingResult<List<VenueDto>>>>() }

    private var page = 1
    private var isGetVenueLoading = false
    private var isLastVenueReceived = false

    fun validForPaging(): Boolean = !isGetVenueLoading && !isLastVenueReceived

    fun getVenueSearch(firstPage: Boolean, search: String) {
        isGetVenueLoading = true
        venueSearch.value = Resource.loading()

        val hashMap = hashMapOf<String, String>()
        if (firstPage)
            hashMap["pageNo"] = 1.toString()
        else
            hashMap["pageNo"] = page.toString()

        if (!search.isNullOrEmpty()) {
            hashMap["search"] = search
        }
        val currentLat = UserManager.getLastLatitude()
        val currentLong = UserManager.getLastLongitude()
        if (currentLat != null)
            hashMap["currentLat"] = currentLat.toString()
        if (currentLong != null)
            hashMap["currentLong"] = currentLong.toString()
        RetrofitClient.conversifyApi
                .getVenueSearch(hashMap)
                .enqueue(object : Callback<ApiResponse<List<VenueDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<VenueDto>>>,
                                            response: Response<ApiResponse<List<VenueDto>>>) {
                        if (response.isSuccessful) {
                            if (firstPage) {
                                // Reset for first page
                                isLastVenueReceived = false
                                page = 1
                            }

                            val receivedGroups = response.body()?.data ?: emptyList()
                            if (receivedGroups.size < PAGE_LIMIT) {
                                Timber.i("Last group is received")
                                isLastVenueReceived = true
                            } else {
                                Timber.i("Next page for topic groups is available")
                                ++page
                            }

                            venueSearch.value = Resource.success(PagingResult(firstPage, receivedGroups))
                        } else {
                            venueSearch.value = Resource.error(response.getAppError())
                        }
                        isGetVenueLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse<List<VenueDto>>>, t: Throwable) {
                        isGetVenueLoading = false
                        venueSearch.value = Resource.error(t.failureAppError())
                    }
                })
    }
}