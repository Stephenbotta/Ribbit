package com.ribbit.ui.main.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.ribbit.data.local.PrefsManager
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.PagingResult
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.survey.GetSurveyData
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class HomeViewModel : ViewModel() {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val homeFeed by lazy { MutableLiveData<Resource<PagingResult<List<GroupPostDto>>>>() }
    val updateDeviceToken by lazy { SingleLiveEvent<Resource<Any>>() }

    private var page = 1
    private var isGetHomeFeedLoading = false
    private var isLastHomeFeedReceived = false

    private var getHomeFeedCall: Call<ApiResponse<List<GroupPostDto>>>? = null

    fun validForPaging(): Boolean = !isGetHomeFeedLoading && !isLastHomeFeedReceived

    fun getHomeFeed(firstPage: Boolean = true, showLoading: Boolean = true) {
        isGetHomeFeedLoading = true
        if (showLoading) {
            homeFeed.value = Resource.loading()
        }

        getHomeFeedCall?.cancel()
        val call = RetrofitClient.ribbitApi
                .getHomeFeed(page = if (firstPage) 1 else page, limit = PAGE_LIMIT)
        call.enqueue(object : Callback<ApiResponse<List<GroupPostDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<GroupPostDto>>>,
                                    response: Response<ApiResponse<List<GroupPostDto>>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastHomeFeedReceived = false
                        page = 1
                    }

                //    Log.d("isIII",response.body()?.data.isSu)
                 //   UserManager.saveProfile(response.body()?.data?.user)

                    val receivedPosts = response.body()?.data ?: emptyList()

                    if (receivedPosts.size < PAGE_LIMIT) {
                        Timber.i("Last home feed post is received")
                        isLastHomeFeedReceived = true
                    } else {
                        Timber.i("Next page for home feed posts is available")
                        ++page
                    }
                    homeFeed.value = Resource.success(PagingResult(firstPage, receivedPosts))
                } else {
                    homeFeed.value = Resource.error(response.getAppError())
                }
                isGetHomeFeedLoading = false
            }

            override fun onFailure(call: Call<ApiResponse<List<GroupPostDto>>>, t: Throwable) {
                isGetHomeFeedLoading = false
                if (!call.isCanceled) {
                    homeFeed.value = Resource.error(t.failureAppError())
                }
            }
        })
    }


    fun updateDeviceToken(deviceToken: String) {
        updateDeviceToken.value = Resource.loading()

        RetrofitClient.ribbitApi
                .updateDeviceToken(deviceToken)
                .enqueue(object : Callback<ApiResponse<GetSurveyData>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyData>>, response: Response<ApiResponse<GetSurveyData>>) {
                        if (response.isSuccessful) {
                            updateDeviceToken.value = Resource.success(response.body()?.data)
                          //  Log.d("fsdsg",response.body()?.data?.isTakeSurvey.toString())
                            val profile = UserManager.getProfile()
                            profile.isTakeSurvey = response.body()?.data?.isTakeSurvey
                            UserManager.saveProfile(profile)
                        } else {
                            updateDeviceToken.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyData>>, t: Throwable) {
                        updateDeviceToken.value = Resource.error(t.failureAppError())
                    }
                })
    }

}