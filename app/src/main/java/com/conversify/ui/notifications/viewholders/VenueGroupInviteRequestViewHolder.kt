package com.conversify.ui.notifications.viewholders

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.extensions.clickSpannable
import com.conversify.utils.AppUtils
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_venue_group_invite_request.view.*
import timber.log.Timber

class VenueGroupInviteRequestViewHolder(itemView: View,
                                        private val glide: GlideRequests,
                                        private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.brandon_text_bold) }

    private val userProfileClickListener = View.OnClickListener {
        Timber.i("User profile clicked : ${notification.sender?.userName}")
    }

    private val venueGroupClickListener = View.OnClickListener {
        if (isRequestForVenue) {
            Timber.i("Venue name clicked : ${notification.venue?.name}")
        } else {
            Timber.i("Group name clicked : ${notification.group?.name}")
        }
    }

    init {
        itemView.btnAccept.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onInviteRequestAction(true, notification)
            }
        }

        itemView.btnReject.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onInviteRequestAction(false, notification)
            }
        }

        itemView.ivProfile.setOnClickListener(userProfileClickListener)
    }

    private lateinit var notification: NotificationDto
    private var isRequestForVenue: Boolean = false

    fun bind(notification: NotificationDto) {
        this.notification = notification
        isRequestForVenue = AppUtils.isRequestForVenue(notification)

        val sender = notification.sender
        glide.load(sender?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(notification.createdOnDateTime, itemView.context)

        val username = sender?.userName ?: ""
        val venueName = if (isRequestForVenue) {
            notification.venue?.name
        } else {
            notification.group?.name
        } ?: ""
        val completeText = itemView.context.getString(R.string.notifications_label_send_request_to_join, username, venueName)

        itemView.tvTitle.setText(completeText, TextView.BufferType.SPANNABLE)

        itemView.tvTitle.clickSpannable(spannableText = username,
                textColorRes = R.color.textGray,
                textTypeface = boldTypeface,
                clickListener = userProfileClickListener)

        itemView.tvTitle.clickSpannable(spannableText = venueName,
                textColorRes = R.color.colorPrimary,
                textTypeface = boldTypeface,
                clickListener = venueGroupClickListener)
    }

    interface Callback {
        fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto)
    }
}