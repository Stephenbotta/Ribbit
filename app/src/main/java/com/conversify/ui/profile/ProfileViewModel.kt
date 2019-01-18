package com.conversify.ui.profile

import android.arch.lifecycle.ViewModel
import com.conversify.data.local.UserManager
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.Resource
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private var profile = UserManager.getProfile()
    val logout by lazy { SingleLiveEvent<Resource<Any>>() }

    fun logout() {
        logout.value = Resource.loading()
        RetrofitClient.conversifyApi
                .logout()
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            logout.value = Resource.success()
                        } else {
                            logout.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        logout.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

    fun profileUpdated() {
        profile = UserManager.getProfile()
    }
}