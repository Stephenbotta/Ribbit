package com.ribbit.ui.main.notifications.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.ribbit.R
import com.ribbit.data.remote.PushType
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.notifications.NotificationDto
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.clickSpannable
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.AppUtils
import com.ribbit.utils.DateTimeUtils
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_notification_normal_with_detail.view.*
import timber.log.Timber

class NormalDetailsViewHolder(itemView: View,
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

        when (notification.type) {

            PushType.ALERT_CONVERSE_NEARBY_PUSH, PushType.ALERT_LOOK_NEARBY_PUSH -> {
                callback.onCrossedPathClicked(notification)
            }
            PushType.ACCEPT_INVITE_VENUE, PushType.ACCEPT_REQUEST_VENUE, PushType.JOINED_VENUE, PushType.VENUE -> {
                Timber.i("Venue name clicked : ${notification.venue?.name}")
                notification.venue?.let { venue ->
                    callback.onVenueClicked(venue)
                }
            }
            PushType.ACCEPT_INVITE_GROUP, PushType.ACCEPT_REQUEST_GROUP, PushType.JOINED_GROUP, PushType.GROUP -> {
                Timber.i("Group name clicked : ${notification.group?.name}")
                notification.group?.let { group ->
                    callback.onGroupClicked(group)
                }
            }
        }
    }

    private val itemClickListener = View.OnClickListener {

        when (notification.type) {
            PushType.FOLLOW -> {
                notification.sender?.let { profile ->
                    callback.onUserProfileClicked(profile)
                }
            }
            PushType.ALERT_CONVERSE_NEARBY_PUSH, PushType.ALERT_LOOK_NEARBY_PUSH -> {
                callback.onCrossedPathClicked(notification)
            }
            else -> {
                notification.postId?.let { groupPostDto ->
                    callback.onGroupPostClicked(groupPostDto)
                }
            }
        }
    }

    init {
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
        val groupName = if (!notification.group?.name.isNullOrEmpty()) {
            notification.group?.name
        } else {
            ""
        } ?: ""
        val venueName = if (!notification.venue?.name.isNullOrEmpty()) {
            notification.venue?.name
        } else {
            ""
        } ?: ""

        val comment = if (!notification.commentId?.comment.isNullOrEmpty()) {
            notification.commentId?.comment
        } else ""

        val address = if (!notification.locationAddress.isNullOrEmpty()) {
            notification.locationAddress
        } else ""

        val locationName = if (!notification.locationName.isNullOrEmpty()) {
            notification.locationName
        } else ""
        val completeText = when (notification.type) {
            PushType.ALERT_LOOK_NEARBY_PUSH -> {
                itemView.context.getString(R.string.notifications_label_cross_path, username, locationName, address)
            }
            PushType.ALERT_CONVERSE_NEARBY_PUSH -> {
                itemView.context.getString(R.string.notifications_label_converse_nearby, username)
            }
            PushType.LIKE -> {
                itemView.context.getString(R.string.notifications_label_like, username)
            }
            PushType.LIKE_POST -> {
                itemView.context.getString(R.string.notifications_label_like, username)
            }
            PushType.LIKE_COMMENT -> {
                itemView.context.getString(R.string.notifications_label_like_comment, username)
            }
            PushType.LIKE_REPLY -> {
                itemView.context.getString(R.string.notifications_label_sub_reply_like, username)
            }
            PushType.REPLY -> {
                itemView.context.getString(R.string.notifications_label_reply, username)
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
            PushType.ACCEPT_INVITE_VENUE -> {
                itemView.context.getString(R.string.notifications_label_accept_invite_venue, username, venueName)
            }
            PushType.ACCEPT_INVITE_GROUP -> {
                itemView.context.getString(R.string.notifications_label_accept_invite, username, groupName)
            }
            PushType.ACCEPT_REQUEST_VENUE -> {
                itemView.context.getString(R.string.notifications_label_accept_request_venue, username, venueName)
            }
            PushType.ACCEPT_REQUEST_GROUP -> {
                itemView.context.getString(R.string.notifications_label_accept_request_channel, username, groupName)
            }
            PushType.ACCEPT_REQUEST_FOLLOW -> {
                itemView.context.getString(R.string.notifications_label_accept_request_follow, username)
            }
            PushType.JOINED_VENUE -> {
                itemView.context.getString(R.string.notifications_label_joined_venue, username, venueName)
            }
            PushType.JOINED_GROUP -> {
                itemView.context.getString(R.string.notifications_label_joined_group, username, groupName)
            }
            PushType.GROUP -> {
                itemView.context.getString(R.string.notifications_label_public_joined_group, username, groupName)
            }
            PushType.VENUE -> {
                itemView.context.getString(R.string.notifications_label_public_joined_venue, username, venueName)
            }
            PushType.FOLLOW -> {
                itemView.context.getString(R.string.notifications_msg_requestFollow, username)
            }
            PushType.POST -> {
                itemView.context.getString(R.string.notifications_label_post, username, venueName)
            }
            else -> {
                ""
            }
        }

        itemView.tvTitle.setText(completeText, TextView.BufferType.SPANNABLE)

        val media = notification.postId?.media?.firstOrNull()
        if (!media?.original.isNullOrEmpty()) {
            itemView.ivCrossPic.visible()
            glide.load(media?.thumbnail)
                    .into(itemView.ivCrossPic)
        } else itemView.ivCrossPic.gone()

        if (completeText.contains(username))
            itemView.tvTitle.clickSpannable(spannableText = username,
                    textColorRes = R.color.textGray,
                    textTypeface = boldTypeface,
                    clickListener = userProfileClickListener)
        if (completeText.contains(address))
            itemView.tvTitle.clickSpannable(spannableText = address,
                    textColorRes = R.color.textGray,
                    clickListener = venueGroupClickListener)
        if (completeText.contains(venueName))
            itemView.tvTitle.clickSpannable(spannableText = venueName,
                    textColorRes = R.color.colorPrimary,
                    textTypeface = boldTypeface,
                    clickListener = venueGroupClickListener)
        if (completeText.contains(groupName))
            itemView.tvTitle.clickSpannable(spannableText = groupName,
                    textColorRes = R.color.colorPrimary,
                    textTypeface = boldTypeface,
                    clickListener = venueGroupClickListener)
    }

    interface Callback {
        fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto)
        fun onUserProfileClicked(profile: ProfileDto)
        fun onGroupPostClicked(groupPost: GroupPostDto)
        fun onCrossedPathClicked(notification: NotificationDto)
        fun onGroupClicked(group: GroupDto)
        fun onVenueClicked(venue: VenueDto)
    }
}