package com.checkIt.ui.people

import android.app.Application
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.people.GetPeopleResponse
import com.checkIt.ui.base.BaseViewModel
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Manish Bhargav
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