package com.conversify.ui.main.notifications.viewholders

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.PushType
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.extensions.clickSpannable
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_normal_with_detail.view.*
import timber.log.Timber

class NormalDetailsViewHolder(itemView: View,
                              private val glide: GlideRequests,
                              private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
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


        val sender = notification.sender
        glide.load(sender?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(notification.createdOnDateTime, itemView.context)

        val username = sender?.userName ?: ""
        val address = notification.postId?.locationAddress ?: ""
        val completeText = when (notification.type) {
            PushType.ALERT_CONVERSE_NEARBY_PUSH -> {
                itemView.context.getString(R.string.notifications_label_cross_path, username, address)
            }
            PushType.ALERT_LOOK_NEARBY_PUSH -> {
                itemView.context.getString(R.string.notifications_label_converse_nearby, username)
            }
            else -> {
                ""
            }
        }

        itemView.tvTitle.setText(completeText, TextView.BufferType.SPANNABLE)

        if (!notification.postId?.imageUrl?.original.isNullOrEmpty()) {
            itemView.ivCrossPic.visible()
            glide.load(notification.postId?.imageUrl?.thumbnail)
                    .into(itemView.ivCrossPic)
        } else itemView.ivCrossPic.gone()

        itemView.tvTitle.clickSpannable(spannableText = username,
                textColorRes = R.color.textGray,
                textTypeface = boldTypeface,
                clickListener = userProfileClickListener)

        itemView.tvTitle.clickSpannable(spannableText = address,
                clickListener = venueGroupClickListener)
    }

    interface Callback {
        fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto)
    }
}