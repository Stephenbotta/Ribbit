package com.ribbit.ui.main

import android.app.Application
import android.location.Geocoder
import android.location.Location
import com.google.gson.Gson
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.RequestCountDto
import com.ribbit.data.remote.socket.SocketManager
import com.ribbit.data.repository.InterestsRepository
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
import io.socket.client.Ack
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class MainViewModel(application: Application) : BaseViewModel(application), CoroutineScope {
    private val socketManager by lazy { SocketManager.getInstance() }
    private val ownUserId by lazy { UserManager.getUserId() }
    private val parentJob by lazy { Job() }
    private val geoCoder by lazy { Geocoder(application) }
    val notificationCount = SingleLiveEvent<String>()
    private val gson by lazy { Gson() }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + parentJob

    private val requestCount = Emitter.Listener { args ->
        Timber.i("Request Count received:\n${args.firstOrNull()}")
        val notificationData = args.firstOrNull()
        if (notificationData is JSONObject) {
            try {
                val requestCountData = gson.fromJson(notificationData.toString(), RequestCountDto::class.java)
                notificationCount.postValue(requestCountData.requestCount ?: "")
//                EventBus.getDefault().post(MessageEvent(SocketManager.EVENT_REQUEST_COUNT))
            } catch (exception: Exception) {
                Timber.w(exception)
            }
        }
    }

    init {
        // Get and cache interests. Callback is not required.
        InterestsRepository.getInstance().getInterests()

        // Connect socket
        socketManager.on(SocketManager.EVENT_REQUEST_COUNT, requestCount)
        socketManager.connect()
    }

    fun currentLocationUpdated(location: Location) {
        launch(Dispatchers.IO) {
            UserManager.updateLocation(location)

            val addresses = try {
                geoCoder.getFromLocation(location.latitude, location.longitude, 1)
            } catch (exception: IOException) {
                Timber.e(exception)
                null
            }

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses.first()

                // Get the first value from lowest level to the highest for location name
                val locationName = address.featureName
                        ?: address.subThoroughfare
                        ?: address.thoroughfare
                        ?: address.subLocality
                        ?: address.locality
                        ?: address.postalCode
                        ?: ""
                val locationAddress = if (address.maxAddressLineIndex != -1) {
                    address.getAddressLine(0)
                } else {
                    ""
                }
                Timber.i(address.toString())

                val arguments = JSONObject()
                arguments.putOpt("locationLat", location.latitude)
                arguments.putOpt("locationLong", location.longitude)
                arguments.putOpt("locationName", locationName)
                arguments.putOpt("locationAddress", locationAddress)
                arguments.putOpt("userId", ownUserId)
                Timber.i(arguments.toString())

                socketManager.emit(SocketManager.EVENT_CURRENT_LOCATION, arguments, Ack {
                    Timber.i("Send current location acknowledgement received")
                })
            }
        }
    }

    fun getNotificationCount() {
        RetrofitClient.ribbitApi.getRequestCounts().enqueue(object : Callback<ApiResponse<RequestCountDto>> {
            override fun onFailure(call: Call<ApiResponse<RequestCountDto>>, t: Throwable) {
            }

            override fun onResponse(call: Call<ApiResponse<RequestCountDto>>, response: Response<ApiResponse<RequestCountDto>>) {
                if (response.isSuccessful) {
                    notificationCount.value = response.body()?.data?.requestCount ?: ""
                }
            }

        })
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.off(SocketManager.EVENT_REQUEST_COUNT, requestCount)
        parentJob.cancel()
    }
}