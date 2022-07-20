package com.ribbit.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.MessageEvent
import com.ribbit.data.remote.PushType
import com.ribbit.ui.main.MainActivity
import com.ribbit.utils.AppConstants
import org.greenrobot.eventbus.EventBus

class MessagingService : FirebaseMessagingService() {
    companion object {
        const val ID = "id"
        const val TYPE = "TYPE"
        const val MESSAGE = "msg"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        UserManager.saveDeviceToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        var conversationId: String? = null
        val notificationTitle = getString(R.string.app_name)
        val channelId = getString(R.string.default_notification_channel_id)
        val data = message.data
        val id = data[ID]
        val type = data[TYPE]
        val msg = data[MESSAGE]
        val currentConversationId = UserManager.getConversationId()

        val intent = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)


        intent.putExtra(ID, id)
        intent.putExtra(TYPE, type)

        when (type) {
            PushType.GROUP_CHAT -> {
                val groupDetails = data["groupDetails"]
                intent.putExtra("data", groupDetails)
                conversationId = id
            }

            PushType.VENUE_CHAT -> {
                val groupDetails = data["groupDetails"]
                intent.putExtra("data", groupDetails)
                conversationId = id
            }

            PushType.CHAT -> {
                val senderDetails = data["senderDetails"]
                intent.putExtra("data", senderDetails)
                conversationId = id
            }
        }

        val pendingIntent = PendingIntent.getActivity(this,
                AppConstants.REQ_CODE_PENDING_INTENT, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (conversationId.isNullOrBlank() || conversationId != currentConversationId) {
            sendNotification(notificationTitle, msg, pendingIntent, channelId, AppConstants.REQ_CODE_PENDING_INTENT)
        }

        //refresh notification count on notification tab
        EventBus.getDefault().post(MessageEvent(AppConstants.EVENT_PUSH_NOTIFICATION))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotification(notificationTitle: String, notificationMsg: String?, pendingIntent: PendingIntent, channelID: String, requestId: Int) {

        val notificationBuilder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_logo_header)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_header)
            notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_logo_header)
        }

        notificationManager.notify(requestId, notificationBuilder.build())
    }
}