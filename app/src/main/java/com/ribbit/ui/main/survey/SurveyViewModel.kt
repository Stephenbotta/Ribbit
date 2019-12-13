package com.ribbit.ui.main.survey

import androidx.lifecycle.ViewModel
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.survey.GetSurveyProperties
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SurveyViewModel : ViewModel() {

    private var profile = GetSurveyProperties()


    val surveyProperties by lazy { SingleLiveEvent<Resource<GetSurveyProperties>>() }

    fun getUserProfileDetails() {
        surveyProperties.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getTakeSurveyProperties()
                .enqueue(object : Callback<ApiResponse<GetSurveyProperties>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyProperties>>, response: Response<ApiResponse<GetSurveyProperties>>) {
                        if (response.isSuccessful) {
                            profile = response.body()?.data ?: GetSurveyProperties()
                            surveyProperties.value = Resource.success(profile)
                        } else {
                            surveyProperties.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyProperties>>, t: Throwable) {
                        surveyProperties.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile
}
