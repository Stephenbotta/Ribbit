package com.ribbit.ui.main.survey

import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.survey.GetSurveyList
import com.ribbit.data.remote.models.survey.GetSurveyProperties
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SurveyViewModel : ViewModel() {

    private var properties = GetSurveyProperties()
    private var list = GetSurveyList()

    val surveyProperties by lazy { SingleLiveEvent<Resource<GetSurveyProperties>>() }

    val surveyList by lazy { SingleLiveEvent<Resource<GetSurveyList>>() }


    fun getSurveyProperties() {
        surveyProperties.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getTakeSurveyProperties()
                .enqueue(object : Callback<ApiResponse<GetSurveyProperties>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyProperties>>, response: Response<ApiResponse<GetSurveyProperties>>) {
                        if (response.isSuccessful) {
                            properties = response.body()?.data ?: GetSurveyProperties()
                            surveyProperties.value = Resource.success(properties)
                        } else {
                            surveyProperties.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyProperties>>, t: Throwable) {
                        surveyProperties.value = Resource.error(t.failureAppError())
                    }
                })
    }


    fun getSurveyList() {
        surveyList.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getSurveyList(1,20)
                .enqueue(object : Callback<ApiResponse<GetSurveyList>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyList>>, response: Response<ApiResponse<GetSurveyList>>) {
                        if (response.isSuccessful) {
                            list = response.body()?.data ?: GetSurveyList()
                            surveyList.value = Resource.success(list)
                        } else {
                            surveyList.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyList>>, t: Throwable) {
                        surveyList.value = Resource.error(t.failureAppError())
                    }
                })
    }


}
