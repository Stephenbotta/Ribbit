package com.ribbit.ui.main.notifications.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.ribbit.R
import com.ribbit.data.remote.PushType
import com.ribbit.data.remote.models.notifications.NotificationDto
import com.ribbit.extensions.clickSpannable
import com.ribbit.extensions.shortToast
import com.ribbit.utils.AppUtils
import com.ribbit.utils.DateTimeUtils
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_normal.view.*
import timber.log.Timber

class NormalViewHolder(itemView: View,
                       private val glide: GlideRequests,
                       private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.roboto_text_bold) }

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
        itemView.ivProfile.setOnClickListener(userProfileClickListener)
    }

    private lateinit var notification: NotificationDto
    private var isRequestForVenue: Boolean = false

    fun bind(notification: NotificationDto) {
        this.notification = notification
        isRequestForVenue = AppUtils.isRequestForVenue(notification)
        itemView.context.shortToast(notification.type ?: "")
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

        val comment = if (!notification.commentId?.comment.isNullOrEmpty()) {
            notification.commentId?.comment
        } else ""

        val completeText = when (notification.type) {

            PushType.LIKE_POST -> {
                itemView.context.getString(R.string.notifications_label_like, username)
            }
            PushType.LIKE_COMMENT -> {
                itemView.context.getString(R.string.notifications_label_like_comment, username)
            }
            PushType.LIKE_REPLY -> {
                itemView.context.getString(R.string.notifications_label_sub_reply_like, username)
            }
            PushType.COMMENT -> {
                itemView.context.getString(R.string.notifications_label_comment, username, comment)
            }
            PushType.TAG_COMMENT -> {
                itemView.context.getString(R.string.notifications_label_tag_comment, username)
            }
            PushType.TAG_REPLY -> {
                itemView.context.getString(R.string.notifications_label_tag_reply, username)
            }
            PushType.ACCEPT_INVITE_VENUE, PushType.ACCEPT_INVITE_GROUP -> {
                itemView.context.getString(R.string.notifications_label_accept_invite, username, venueName)
            }
            PushType.ACCEPT_REQUEST_VENUE, PushType.ACCEPT_REQUEST_GROUP -> {
                itemView.context.getString(R.string.notifications_label_accept_request_channel, username, venueName)
            }
            PushType.ACCEPT_REQUEST_FOLLOW -> {
                itemView.context.getString(R.string.notifications_label_accept_request_follow, username)
            }
            PushType.JOINED_VENUE, PushType.JOINED_GROUP -> {
                itemView.context.getString(R.string.notifications_label_joined_group, username, venueName)
            }
            PushType.REPLY -> {
                itemView.context.getString(R.string.notifications_label_tag_comment, username)
            }
            else -> {
                ""
            }

        }

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