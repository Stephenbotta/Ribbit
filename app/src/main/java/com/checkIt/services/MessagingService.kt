package com.checkIt.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.checkIt.R
import com.checkIt.data.local.PrefsManager
import com.checkIt.data.local.UserManager
import com.checkIt.data.local.models.MessageEvent
import com.checkIt.data.remote.PushType
import com.checkIt.ui.main.MainActivity
import com.checkIt.utils.AppConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus

/**
 * Created by Manish Bhargav
 */
class MessagingService : FirebaseMessagingService() {

    companion object {
        const val ID = "id"
        const val TYPE = "TYPE"
        const val MESSAGE = "msg"
    }

    private var chatType = ""

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        UserManager.saveDeviceToken(token.toString())
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        val notificationTitle = getString(R.string.app_name)
        val data = message?.data ?: emptyMap()
        val type = data[TYPE]
        val msg = data[MESSAGE]
        var isChatOpen = false

        val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        when (type) {

            PushType.LIKE, PushType.LIKE_POST, PushType.REPLY, PushType.LIKE_REPLY,
            PushType.COMMENT, PushType.LIKE_COMMENT, PushType.VENUE, PushType.GROUP,
            PushType.REQUEST_VENUE, PushType.REQUEST_GROUP, PushType.FOLLOW, PushType.POST,
            PushType.TAG_COMMENT, PushType.TAG_REPLY, PushType.REQUEST_FOLLOW,
            PushType.ACCEPT_REQUEST_FOLLOW, PushType.ALERT_CONVERSE_NEARBY_PUSH,
            PushType.ALERT_LOOK_NEARBY_PUSH, PushType.JOINED_VENUE, PushType.JOINED_GROUP,
            PushType.ACCEPT_REQUEST_GROUP, PushType.ACCEPT_REQUEST_VENUE,
            PushType.ACCEPT_INVITE_GROUP, PushType.ACCEPT_INVITE_VENUE,
            PushType.INVITE_VENUE, PushType.INVITE_GROUP -> {
                val id = data[ID]
//                val byId = data["byId"]
                intent.putExtra(ID, id)
                intent.putExtra(TYPE, type)
            }
            PushType.GROUP_CHAT -> {
                val id = data[ID]
                chatType = "GROUP"
                val groupDetails = data["groupDetails"]
                intent.putExtra(ID, id)
                intent.putExtra(TYPE, type)
                intent.putExtra("data", groupDetails)
                isChatOpen = PrefsManager.get().getBoolean(PrefsManager.PREF_IS_CHAT_OPEN, false)
            }

            PushType.VENUE_CHAT -> {
                val id = data[ID]
                chatType = "VENUE"
                val groupDetails = data["groupDetails"]
                intent.putExtra(ID, id)
                intent.putExtra(TYPE, type)
                intent.putExtra("data", groupDetails)
                isChatOpen = PrefsManager.get().getBoolean(PrefsManager.PREF_IS_CHAT_OPEN, false)
            }

            PushType.CHAT -> {
                val id = data[ID]
                chatType = "INDIVIDUAL"
                val senderDetails = data["senderDetails"]
                intent.putExtra(ID, id)
                intent.putExtra(TYPE, type)
                intent.putExtra("data", senderDetails)
                isChatOpen = PrefsManager.get().getBoolean(PrefsManager.PREF_IS_CHAT_OPEN, false)
            }
        }

        val pendingIntent = PendingIntent.getActivity(this, AppConstants.REQ_CODE_PENDING_INTENT,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (!isChatOpen)
            sendNotification(notificationTitle, msg, pendingIntent, "11", AppConstants.REQ_CODE_PENDING_INTENT)
        else {
            if (chatType == PrefsManager.get().getString(PrefsManager.PREF_IS_CHAT_TYPE, "")) {

            } else {
                sendNotification(notificationTitle, msg, pendingIntent, "11", AppConstants.REQ_CODE_PENDING_INTENT)
            }
        }
        EventBus.getDefault().post(MessageEvent(AppConstants.EVENT_PUSH_NOTIFICATION))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotification(notificationTitle: String, notificationMsg: String?, pendingIntent: PendingIntent, channelID: String, requestId: Int) {

        val notificationBuilder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(notificationIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMsg))
                .setContentTitle(notificationTitle) //Header
                .setContentText(notificationMsg) //Content
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        // notificationBuilder.flags = notificationBuilder.flags or Notification.FLAG_AUTO_CANCEL

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelID, getText(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(notificationIcon)
            notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        } else {
            notificationBuilder.setSmallIcon(notificationIcon)
        }

        notificationManager.notify(requestId, notificationBuilder.build())
    }

    private val notificationIcon: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            R.drawable.ic_logo_header
        } else {
            R.drawable.ic_logo_header
        }

}