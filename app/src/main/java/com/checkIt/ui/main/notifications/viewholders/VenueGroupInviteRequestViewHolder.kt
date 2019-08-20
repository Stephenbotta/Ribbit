package com.checkIt.ui.main.notifications.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.R
import com.checkIt.data.remote.PushType
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.notifications.NotificationDto
import com.checkIt.data.remote.models.venues.VenueDto
import com.checkIt.extensions.clickSpannable
import com.checkIt.utils.AppUtils
import com.checkIt.utils.DateTimeUtils
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_venue_group_invite_request.view.*
import timber.log.Timber

class VenueGroupInviteRequestViewHolder(itemView: View,
                                        private val glide: GlideRequests,
                                        private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.roboto_text_bold) }

    private val userProfileClickListener = View.OnClickListener {
        Timber.i("User profile clicked : ${notification.sender?.userName}")
        notification.sender?.let { profile ->
            callback.onUserProfileClicked(profile)
        }
    }

    private val venueGroupClickListener = View.OnClickListener {
        if (isRequestForVenue) {
            Timber.i("Venue name clicked : ${notification.venue?.name}")
//            notification.venue?.let { venue ->
//                callback.onVenueClicked(venue)
//            }
        } else {
            Timber.i("Group name clicked : ${notification.group?.name}")
        }
    }

    private val itemClickListener = View.OnClickListener {

        when (notification.type) {
            PushType.REQUEST_FOLLOW -> {
                notification.sender?.let { profile ->
                    callback.onUserProfileClicked(profile)
                }
            }
        }
    }

    init {
        itemView.btnAccept.setOnClickListener {
            if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                if (notification.type == PushType.REQUEST_FOLLOW) {
                    callback.onFollowRequestAction(true, notification)
                } else {
                    callback.onInviteRequestAction(true, notification)
                }
            }
        }

        itemView.btnReject.setOnClickListener {
            if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                if (notification.type == PushType.REQUEST_FOLLOW) {
                    callback.onFollowRequestAction(false, notification)
                } else {
                    callback.onInviteRequestAction(false, notification)
                }
            }
        }
        itemView.ivProfile.setOnClickListener(userProfileClickListener)
        itemView.setOnClickListener(itemClickListener)
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

        val completeText = when (notification.type) {

            PushType.REQUEST_FOLLOW -> {
                itemView.context.getString(R.string.notifications_msg_requestFollowPrivate, username)
            }
            PushType.REQUEST_GROUP -> {
                itemView.context.getString(R.string.notifications_label_send_request_to_join, username, venueName)
            }
            PushType.REQUEST_VENUE -> {
                itemView.context.getString(R.string.notifications_label_send_request_to_join_venue, username, venueName)
            }
            PushType.INVITE_VENUE -> {
                itemView.context.getString(R.string.notifications_label_invite_venue, username, venueName)
            }
            PushType.INVITE_GROUP -> {
                itemView.context.getString(R.string.notifications_label_invite, username, venueName)
            }
            else -> {
                ""
            }
        }


        itemView.tvTitle.setText(completeText, TextView.BufferType.SPANNABLE)

        if (completeText.contains(username))
            itemView.tvTitle.clickSpannable(spannableText = username,
                    textColorRes = R.color.textGray,
                    textTypeface = boldTypeface,
                    clickListener = userProfileClickListener)
        if (completeText.contains(venueName))
            itemView.tvTitle.clickSpannable(spannableText = venueName,
                    textColorRes = R.color.colorPrimary,
                    textTypeface = boldTypeface,
                    clickListener = venueGroupClickListener)
    }

    interface Callback {
        fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto)
        fun onUserProfileClicked(profile: ProfileDto)
        fun onGroupPostClicked(groupPost: GroupPostDto)
        fun onFollowRequestAction(action: Boolean, notification: NotificationDto)
        fun onGroupClicked(group: GroupDto)
        fun onVenueClicked(venue: VenueDto)
    }
}