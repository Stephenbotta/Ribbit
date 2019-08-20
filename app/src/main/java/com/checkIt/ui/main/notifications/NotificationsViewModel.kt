package com.checkIt.ui.main.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.PagingResult
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.notifications.NotificationDto
import com.checkIt.utils.AppUtils
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class NotificationsViewModel : ViewModel() {
    companion object {
        private const val PAGE_LIMIT = 10
    }

    val notifications by lazy { MutableLiveData<Resource<PagingResult<List<NotificationDto>>>>() }
    val joinVenueRequest by lazy { SingleLiveEvent<Resource<NotificationDto>>() }

    private var page = 1
    private var isGetNotificationsLoading = false
    private var isLastNotificationReceived = false

    private var getNotificationsCall: Call<ApiResponse<List<NotificationDto>>>? = null

    fun validForPaging(): Boolean = !isGetNotificationsLoading && !isLastNotificationReceived

    fun getNotifications(firstPage: Boolean = true) {
        isGetNotificationsLoading = true
        notifications.value = Resource.loading()

        val call = RetrofitClient.conversifyApi
                .getNotifications(if (firstPage) 1 else page)

        // Cancel any on-going call
        getNotificationsCall?.cancel()
        getNotificationsCall = call

        call.enqueue(object : Callback<ApiResponse<List<NotificationDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<NotificationDto>>>,
                                    response: Response<ApiResponse<List<NotificationDto>>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastNotificationReceived = false
                        page = 1
                    }

                    /*
                    * todo - currently only displaying the venue and group join requests.
                    * Use "receivedNotifications" list later and remove "venueJoinRequestNotifications".
                    * */
                    val receivedNotifications = response.body()?.data ?: emptyList()
                    val venueJoinRequestNotifications = receivedNotifications.filter {
                        it.type == ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_VENUE ||
                                it.type == ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_GROUP
                    }
                    if (receivedNotifications.size < PAGE_LIMIT) {
                        Timber.i("Last notification is received")
                        isLastNotificationReceived = true
                    } else {
                        Timber.i("Next page for notifications is available")
                        ++page
                    }

                    notifications.value = Resource.success(PagingResult(firstPage, receivedNotifications))
                } else {
                    notifications.value = Resource.error(response.getAppError())
                }
                isGetNotificationsLoading = false
            }

            override fun onFailure(call: Call<ApiResponse<List<NotificationDto>>>, t: Throwable) {
                isGetNotificationsLoading = false
                if (!call.isCanceled) {
                    notifications.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun clearNotifications() {
        isGetNotificationsLoading = true
        notifications.value = Resource.loading()

        val call = RetrofitClient.conversifyApi
                .clearNotification()

        // Cancel any on-going call
        getNotificationsCall?.cancel()
        getNotificationsCall = call

        call.enqueue(object : Callback<ApiResponse<List<NotificationDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<NotificationDto>>>,
                                    response: Response<ApiResponse<List<NotificationDto>>>) {
                if (response.isSuccessful) {
                    // Reset for first page
                    isLastNotificationReceived = false
                    page = 1

                    /*
                    * todo - currently only displaying the venue and group join requests.
                    * Use "receivedNotifications" list later and remove "venueJoinRequestNotifications".
                    * */
                    val receivedNotifications = response.body()?.data ?: emptyList()
                    if (receivedNotifications.size < PAGE_LIMIT) {
                        Timber.i("Last notification is received")
                        isLastNotificationReceived = true
                    } else {
                        Timber.i("Next page for notifications is available")
                        ++page
                    }

                    notifications.value = Resource.success(PagingResult(true, receivedNotifications))
                } else {
                    notifications.value = Resource.error(response.getAppError())
                }
                isGetNotificationsLoading = false
            }

            override fun onFailure(call: Call<ApiResponse<List<NotificationDto>>>, t: Throwable) {
                isGetNotificationsLoading = false
                if (!call.isCanceled) {
                    notifications.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun acceptRejectInviteRequest(acceptRequest: Boolean, notification: NotificationDto) {
        joinVenueRequest.value = Resource.loading()

        val acceptType = getAcceptType(notification)
        val groupType = getGroupType(notification)
        val userId = notification.sender?.id ?: ""
        val groupId = if (AppUtils.isRequestForVenue(notification)) {
            notification.venue?.id
        } else {
            notification.group?.id
        } ?: ""

        RetrofitClient.conversifyApi
                .acceptRejectInviteRequest(acceptType, groupType, userId, groupId, acceptRequest)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            joinVenueRequest.value = Resource.success(notification)
                        } else {
                            joinVenueRequest.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        joinVenueRequest.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun acceptFollowRequest(acceptRequest: Boolean, notification: NotificationDto) {
        joinVenueRequest.value = Resource.loading()

        val acceptType = "FOLLOW"
        val groupType = getGroupType(notification)
        val userId = notification.sender?.id ?: ""
        val groupId = if (AppUtils.isRequestForVenue(notification)) {
            notification.venue?.id
        } else {
            notification.group?.id
        } ?: ""

        RetrofitClient.conversifyApi
                .acceptFollowRequest(acceptType, userId, acceptRequest)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            joinVenueRequest.value = Resource.success(notification)
                        } else {
                            joinVenueRequest.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        joinVenueRequest.value = Resource.error(t.failureAppError())
                    }
                })
    }

    private fun getAcceptType(notification: NotificationDto): String {
        return when (notification.type) {
            ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_VENUE,
            ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_GROUP ->
                ApiConstants.ACCEPT_TYPE_REQUEST

            ApiConstants.NOTIFICATION_TYPE_INVITE_JOIN_VENUE,
            ApiConstants.NOTIFICATION_TYPE_INVITE_JOIN_GROUP ->
                ApiConstants.ACCEPT_TYPE_INVITE

            else -> ""
        }
    }

    private fun getGroupType(notification: NotificationDto): String {
        return when (notification.type) {
            ApiConstants.NOTIFICATION_TYPE_INVITE_JOIN_GROUP,
            ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_GROUP ->
                ApiConstants.TYPE_GROUP


            ApiConstants.NOTIFICATION_TYPE_INVITE_JOIN_VENUE,
            ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_VENUE ->
                ApiConstants.TYPE_VENUE

            else -> ""
        }
    }
}