package com.ribbit.ui.loginsignup.chooseinterests

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.data.repository.InterestsRepository
import com.ribbit.utils.SingleLiveEvent
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

        RetrofitClient.ribbitApi
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