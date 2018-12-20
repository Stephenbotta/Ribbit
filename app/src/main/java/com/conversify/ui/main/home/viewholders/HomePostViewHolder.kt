package com.conversify.ui.main.home.viewholders

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.CustomTypefaceSpan
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_home_feed_post.view.*

class HomePostViewHolder(itemView: View,
                         private val glide: GlideRequests,
                         callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.brandon_text_bold) }
    private val usernameColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }

    init {
        itemView.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onPostClicked(post)
            }
        }

        itemView.ivLike.setOnClickListener { }
    }

    private lateinit var post: GroupPostDto

    fun bind(post: GroupPostDto) {
        this.post = post

        glide.load(post.user?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(post.createdOnDateTime, itemView.context)
        itemView.tvMessage.text = post.postText

        val username = post.user?.userName ?: ""
        val groupName = post.group?.name ?: ""
        val applyGroupNameSpannable = !groupName.isBlank()  // Only applied if group is available
        val completeText = if (applyGroupNameSpannable) {
            itemView.context.getString(R.string.home_label_username_with_group_name, username, groupName)
        } else {
            username
        }
        val usernameStartIndex = completeText.indexOf(username)
        val usernameEndIndex = usernameStartIndex + username.length

        val usernameBoldSpannable = CustomTypefaceSpan("", boldTypeface)

        val spannableString = SpannableString(completeText)
        spannableString.setSpan(usernameBoldSpannable, usernameStartIndex, usernameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (applyGroupNameSpannable) {
            val groupNameStartIndex = completeText.indexOf(groupName)
            val groupNameEndIndex = groupNameStartIndex + groupName.length

            val groupNameBoldSpannable = CustomTypefaceSpan("", boldTypeface)
            val foregroundColorSpan = ForegroundColorSpan(usernameColor)

            spannableString.setSpan(groupNameBoldSpannable, groupNameStartIndex, groupNameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(foregroundColorSpan, groupNameStartIndex, groupNameEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        itemView.tvUserName.setText(spannableString, TextView.BufferType.SPANNABLE)

        // Image is only visible when post type is image
        if (post.type == ApiConstants.GROUP_POST_TYPE_IMAGE) {
            itemView.ivImage.visible()
            glide.load(post.imageUrl?.thumbnail)
                    .into(itemView.ivImage)
        } else {
            itemView.ivImage.gone()
        }

        itemView.tvRepliesLikes.text = getFormattedRepliesAndLikes(post, itemView.context)
    }

    private fun getFormattedRepliesAndLikes(post: GroupPostDto, context: Context): String {
        val repliesCount = post.commentsCount ?: 0
        val formattedReplies = context.resources.getQuantityString(R.plurals.replies_with_count, repliesCount, repliesCount)

        val likesCount = post.likesCount ?: 0
        val formattedLikes = context.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)

        // e.g. "156 Replies · 156 Likes"
        return String.format("%s · %s", formattedReplies, formattedLikes)
    }

    interface Callback {
        fun onPostClicked(post: GroupPostDto)
    }
}