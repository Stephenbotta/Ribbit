package com.conversify.ui.main.notifications

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.PushType
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.extensions.inflate
import com.conversify.ui.main.notifications.viewholders.NormalDetailsViewHolder
import com.conversify.ui.main.notifications.viewholders.NormalViewHolder
import com.conversify.ui.main.notifications.viewholders.VenueGroupInviteRequestViewHolder
import com.conversify.utils.GlideRequests

class NotificationsAdapter(private val glide: GlideRequests,
                           private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val NOTIFICATION_TYPE_VENUE = 0
        private const val NOTIFICATION_TYPE_INVITE_REQUEST = 1
        private const val NOTIFICATION_TYPE_CROSSED_PATH = 2
    }

    private val notifications = mutableListOf<NotificationDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NOTIFICATION_TYPE_VENUE ->
                NormalDetailsViewHolder(parent.inflate(R.layout.item_notification_normal_with_detail), glide, callback)
            NOTIFICATION_TYPE_INVITE_REQUEST ->
                VenueGroupInviteRequestViewHolder(parent.inflate(R.layout.item_notification_venue_group_invite_request), glide, callback)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = notifications[position]

        when (holder) {

            is NormalDetailsViewHolder -> {
                holder.bind(notification)
            }

            is VenueGroupInviteRequestViewHolder -> {
                holder.bind(notification)
            }

            is NormalViewHolder -> {
                holder.bind(notification)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (notifications[position].type) {
            PushType.LIKE, PushType.LIKE_REPLY, PushType.LIKE_POST, PushType.COMMENT,
            PushType.REPLY, PushType.LIKE_COMMENT, PushType.VENUE, PushType.GROUP,
            PushType.ACCEPT_INVITE_GROUP, PushType.ACCEPT_INVITE_VENUE,
            PushType.ACCEPT_REQUEST_FOLLOW, PushType.ACCEPT_REQUEST_GROUP,
            PushType.ACCEPT_REQUEST_VENUE, PushType.FOLLOW, PushType.POST,
            PushType.TAG_COMMENT, PushType.TAG_REPLY, PushType.JOINED_VENUE,
            PushType.JOINED_GROUP, PushType.ALERT_CONVERSE_NEARBY_PUSH,
            PushType.ALERT_LOOK_NEARBY_PUSH-> {
                NOTIFICATION_TYPE_VENUE
            }
            PushType.REQUEST_FOLLOW, PushType.REQUEST_GROUP, PushType.REQUEST_VENUE,
            PushType.INVITE_VENUE, PushType.INVITE_GROUP-> {
                NOTIFICATION_TYPE_INVITE_REQUEST
            }
            else -> {
                throw IllegalArgumentException("invalid View type")
//                NOTIFICATION_TYPE_CROSSED_PATH
            }
        }
    }

    fun displayNotifications(notifications: List<NotificationDto>) {
        this.notifications.clear()
        this.notifications.addAll(notifications)
        notifyDataSetChanged()
    }

    fun addNotifications(notifications: List<NotificationDto>) {
        val oldListSize = this.notifications.size
        this.notifications.addAll(notifications)
        notifyItemRangeInserted(oldListSize, notifications.size)
    }

    fun removeNotification(notification: NotificationDto) {
        val index = notifications.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            notifications.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    interface Callback : VenueGroupInviteRequestViewHolder.Callback, NormalDetailsViewHolder.Callback
}