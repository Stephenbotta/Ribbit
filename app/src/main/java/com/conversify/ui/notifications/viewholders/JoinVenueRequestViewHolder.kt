package com.conversify.ui.notifications.viewholders

import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.utils.CustomTypefaceSpan
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_join_venue_request.view.*

class JoinVenueRequestViewHolder(itemView: View,
                                 private val glide: GlideRequests,
                                 private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.brandon_text_bold) }
    private val usernameColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }

    init {
        itemView.btnAccept.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onJoinVenueRequestAction(true, notification)
            }
        }
        itemView.btnReject.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onJoinVenueRequestAction(false, notification)
            }
        }
    }

    private lateinit var notification: NotificationDto

    fun bind(notification: NotificationDto) {
        this.notification = notification

        val sender = notification.sender
        glide.load(sender?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(notification.createdOnDateTime, itemView.context)

        val username = sender?.userName ?: ""
        val venueName = notification.venue?.name ?: ""
        val completeText = itemView.context.getString(R.string.notifications_label_send_request_to_join, username, venueName)
        val usernameStartIndex = completeText.indexOf(username)
        val usernameEndIndex = usernameStartIndex + username.length
        val venueNameStartIndex = completeText.indexOf(venueName)
        val venueNameEndIndex = venueNameStartIndex + venueName.length

        val usernameBoldSpannable = CustomTypefaceSpan("", boldTypeface)
        val venueNameBoldSpannable = CustomTypefaceSpan("", boldTypeface)
        val foregroundColorSpan = ForegroundColorSpan(usernameColor)
        val spannableString = SpannableString(completeText)
        spannableString.setSpan(usernameBoldSpannable, usernameStartIndex, usernameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(venueNameBoldSpannable, venueNameStartIndex, venueNameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(foregroundColorSpan, venueNameStartIndex, venueNameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        itemView.tvTitle.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    interface Callback {
        fun onJoinVenueRequestAction(acceptRequest: Boolean, notification: NotificationDto)
    }
}