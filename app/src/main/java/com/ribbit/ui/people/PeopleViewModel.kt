package com.ribbit.ui.people

import android.app.Application
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.people.GetPeopleResponse
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
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

        RetrofitClient.ribbitApi
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