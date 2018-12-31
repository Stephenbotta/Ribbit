package com.conversify.ui.notifications

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.extensions.inflate
import com.conversify.ui.notifications.viewholders.VenueGroupInviteRequestViewHolder
import com.conversify.utils.GlideRequests

class NotificationsAdapter(private val glide: GlideRequests,
                           private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_VENUE_GROUP_INVITE_REQUEST = 0
    }

    private val notifications = mutableListOf<NotificationDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VENUE_GROUP_INVITE_REQUEST ->
                VenueGroupInviteRequestViewHolder(parent.inflate(R.layout.item_notification_venue_group_invite_request), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = notifications[position]

        when (holder) {
            is VenueGroupInviteRequestViewHolder -> {
                holder.bind(notification)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // todo add other types when added in the layout
        return TYPE_VENUE_GROUP_INVITE_REQUEST
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

    interface Callback : VenueGroupInviteRequestViewHolder.Callback
}