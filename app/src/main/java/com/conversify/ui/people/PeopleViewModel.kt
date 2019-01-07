package com.conversify.ui.people

import android.app.Application
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.people.GetPeopleResponse
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav on 3/1/19.
 */
class PeopleViewModel(application: Application) : BaseViewModel(application) {


    val crossedPeople by lazy { SingleLiveEvent<Resource<List<Any>>>() }

    fun getCrossedPeople() {
        crossedPeople.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getCrossedPeople()
                .enqueue(object : Callback<ApiResponse<List<GetPeopleResponse>>> {
                    override fun onResponse(call: Call<ApiResponse<List<GetPeopleResponse>>>,
                                            response: Response<ApiResponse<List<GetPeopleResponse>>>) {
                        if (response.isSuccessful) {
                            val crossedPeoplePlaces = response.body()?.data ?: emptyList()
                            val items = mutableListOf<Any>()
                            crossedPeoplePlaces.forEach { place ->
                                items.add(place)
                                place.userCrossed?.let { users ->
                                    items.addAll(users)
                                }
                            }
                            crossedPeople.value = Resource.success(items)
                        } else {
                            crossedPeople.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<GetPeopleResponse>>>, t: Throwable) {
                        crossedPeople.value = Resource.error(t.failureAppError())
                    }
                })
    }


}