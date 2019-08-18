package com.checkIt.ui.loginsignup.chooseinterests

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.checkIt.data.local.UserManager
import com.checkIt.data.local.models.AppError
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.repository.InterestsRepository
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChooseInterestsViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }
    val updateInterests by lazy { SingleLiveEvent<Resource<List<InterestDto>>>() }
    val myInterestIds by lazy {
        ArrayList<String>()
    }

    private val interestsRepository by lazy { InterestsRepository.getInstance() }

    fun hasInterests(): Boolean = interestsRepository.hasCachedInterests()

    fun getInterests() {
        if (hasInterests()) {
            interests.value = Resource.success(interestsRepository.getCachedInterests())
            return
        }

        interestsRepository.getInterests(object : InterestsRepository.GetInterestsCallback {
            override fun onGetInterestsLoading() {
                interests.value = Resource.loading()
            }

            override fun onGetInterestsSuccess(allInterests: List<InterestDto>) {
                interests.value = Resource.success(allInterests)
            }

            override fun onGetInterestsFailed(error: AppError) {
                interests.value = Resource.error(error)
            }
        })
    }

    fun updateInterests(selectedInterestIds: List<String>, updateInPref: Boolean = true) {
        updateInterests.value = Resource.loading()

        RetrofitClient.conversifyApi
                .updateInterests(selectedInterestIds)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null && updateInPref) {
                                UserManager.saveProfile(profile)
                            }
                            updateInterests.value = Resource.success(profile?.interests
                                    ?: arrayListOf())
                        } else {
                            updateInterests.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        updateInterests.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun start(interest: ArrayList<InterestDto>) {
        myInterestIds.addAll(interest.mapNotNull { it.id }.toSet())
    }
}