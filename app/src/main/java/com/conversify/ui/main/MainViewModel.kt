package com.conversify.ui.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Geocoder
import android.location.Location
import com.conversify.data.local.UserManager
import com.conversify.data.remote.socket.SocketManager
import com.conversify.data.repository.InterestsRepository
import io.socket.client.Ack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class MainViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    private val socketManager by lazy { SocketManager.getInstance() }
    private val ownUserId by lazy { UserManager.getUserId() }
    private val parentJob by lazy { Job() }
    private val geoCoder by lazy { Geocoder(application) }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + parentJob

    init {
        // Get and cache interests. Callback is not required.
        InterestsRepository.getInstance().getInterests()

        // Connect socket
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

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}