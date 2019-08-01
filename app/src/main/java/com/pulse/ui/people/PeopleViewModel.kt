package com.pulse.ui.people

import android.app.Application
import com.pulse.data.remote.RetrofitClient
import com.pulse.data.remote.failureAppError
import com.pulse.data.remote.getAppError
import com.pulse.data.remote.models.ApiResponse
import com.pulse.data.remote.models.Resource
import com.pulse.data.remote.models.people.GetPeopleResponse
import com.pulse.ui.base.BaseViewModel
import com.pulse.utils.SingleLiveEvent
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