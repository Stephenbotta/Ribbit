package com.ribbit.ui.main.survey

import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.survey.GetSurveyInfo
import com.ribbit.data.remote.models.survey.GetSurveyList
import com.ribbit.data.remote.models.survey.GetSurveyProperties
import com.ribbit.data.remote.models.survey.RequestSurveyProperties
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SurveyViewModel : ViewModel() {
    val surveyProperties by lazy { SingleLiveEvent<Resource<GetSurveyProperties>>() }
    val takeSurveyProperties by lazy { SingleLiveEvent<Resource<Any>>() }
    val surveyList by lazy { SingleLiveEvent<Resource<GetSurveyList>>() }
    val surveyInfo by lazy { SingleLiveEvent<Resource<GetSurveyInfo>>() }
    val submitSurvey by lazy { SingleLiveEvent<Resource<Any>>() }

    fun getSurveyProperties() {
        surveyProperties.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getTakeSurveyProperties()
                .enqueue(object : Callback<ApiResponse<GetSurveyProperties>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyProperties>>, response: Response<ApiResponse<GetSurveyProperties>>) {
                        if (response.isSuccessful) {
                            surveyProperties.value = Resource.success(response.body()?.data
                                    ?: GetSurveyProperties())
                        } else {
                            surveyProperties.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyProperties>>, t: Throwable) {
                        surveyProperties.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun takeSurveyProperties(model: RequestSurveyProperties) {
        takeSurveyProperties.value = Resource.loading()

        RetrofitClient.ribbitApi
                .takeSurveyProperties(model)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {

                            takeSurveyProperties.value = Resource.success("done")
                        } else {
                            takeSurveyProperties.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        takeSurveyProperties.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getSurveyList() {
        surveyList.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getSurveyList(1, 20)
                .enqueue(object : Callback<ApiResponse<GetSurveyList>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyList>>, response: Response<ApiResponse<GetSurveyList>>) {
                        if (response.isSuccessful) {
                            surveyList.value = Resource.success(response.body()?.data
                                    ?: GetSurveyList())
                        } else {
                            surveyList.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyList>>, t: Throwable) {
                        surveyList.value = Resource.error(t.failureAppError())
                    }
                })
    }


    fun getQuestionList(surveyId: String) {
        surveyInfo.value = Resource.loading()

        RetrofitClient.ribbitApi
                .getSurveyQuestions(surveyId)
                .enqueue(object : Callback<ApiResponse<GetSurveyInfo>> {
                    override fun onResponse(call: Call<ApiResponse<GetSurveyInfo>>, response: Response<ApiResponse<GetSurveyInfo>>) {
                        if (response.isSuccessful) {
                            surveyInfo.value = Resource.success(response.body()?.data)
                        } else {
                            surveyInfo.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<GetSurveyInfo>>, t: Throwable) {
                        surveyInfo.value = Resource.error(t.failureAppError())
                    }
                })
    }


    fun submitSurveyQuiz(surveyId: String, data: String) {
        submitSurvey.value = Resource.loading()
        //todo check for this at last
        RetrofitClient.ribbitApi
                .submitSurvey(surveyId, data)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            submitSurvey.value = Resource.success()
                        } else {
                            submitSurvey.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        submitSurvey.value = Resource.error(t.failureAppError())
                    }
                })
    }


}
