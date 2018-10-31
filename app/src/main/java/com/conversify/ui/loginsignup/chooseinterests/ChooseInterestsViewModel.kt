package com.conversify.ui.loginsignup.chooseinterests

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChooseInterestsViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }

    fun getInterests() {
        interests.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getInterests()
                .enqueue(object : Callback<ApiResponse<List<InterestDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<InterestDto>>>,
                                            response: Response<ApiResponse<List<InterestDto>>>) {
                        if (response.isSuccessful) {
                            val allInterests = response.body()?.data ?: emptyList()
                            interests.value = Resource.success(allInterests)
                        } else {
                            interests.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<InterestDto>>>, t: Throwable) {
                        interests.value = Resource.error(t.failureAppError())
                    }
                })
    }
}