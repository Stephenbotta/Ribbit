package com.conversify.ui.main.home.viewholders

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.*
import com.conversify.ui.groups.GroupPostCallback
import com.conversify.utils.AppUtils
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import com.conversify.utils.SpannableTextClickListener
import kotlinx.android.synthetic.main.item_home_feed_post.view.*

class HomePostViewHolder(itemView: View,
                         private val glide: GlideRequests,
                         callback: GroupPostCallback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.brandon_text_bold) }
    private val postClickListener = View.OnClickListener {
        callback.onPostClicked(post, false)
    }
    private val likesCountClickListener = View.OnClickListener {
        callback.onLikesCountClicked(post)
    }
    private val userProfileClickListener = View.OnClickListener {
        post.user?.let { profile ->
            callback.onUserProfileClicked(profile)
        }
    }
    private val groupNameClickListener = View.OnClickListener {
        post.group?.let { group ->
            callback.onGroupClicked(group)
        }
    }
    private val hashtagClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
            callback.onHashtagClicked(text)
        }
    }
    private val usernameClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
            callback.onUsernameMentionClicked(text)
        }
    }

    init {
        itemView.tvUserName.setOnClickListener {
            if (itemView.tvUserName.isNonLinkClick()) {
                // Forward click to the post click listener.
                postClickListener.onClick(it)
            }
        }

        itemView.setOnClickListener(postClickListener)
        itemView.tvMessage.setOnClickListener {
            if (itemView.tvMessage.isNonLinkClick()) {
                // Forward click to the post click listener.
                postClickListener.onClick(it)
            }
        }

        itemView.ivLike.setOnClickListener {
            if (isValidPosition() && itemView.context.isNetworkActive()) {
                val isLiked = !(post.isLiked ?: false)     // toggle liked state
                post.isLiked = isLiked

                val currentLikesCount = post.likesCount ?: 0
                post.likesCount = if (isLiked) {
                    currentLikesCount + 1
                } else {
                    currentLikesCount - 1
                }
                updateRepliesAndLikes()
                updateLikeButtonState()
                callback.onGroupPostLikeClicked(post, isLiked)
            }
        }

        itemView.ivReply.setOnClickListener {
            callback.onPostClicked(post, true)
        }

        itemView.ivProfile.setOnClickListener(userProfileClickListener)
    }

    private lateinit var post: GroupPostDto

    fun bind(post: GroupPostDto) {
        this.post = post

        glide.load(post.user?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(post.createdOnDateTime, itemView.context)

        val message = post.postText ?: ""
        itemView.tvMessage.text = message

        updateLikeButtonState()

        // Image is only visible when post type is image
        if (post.type == ApiConstants.GROUP_POST_TYPE_IMAGE) {
            itemView.ivImage.visible()
            glide.load(post.imageUrl?.thumbnail)
                    .into(itemView.ivImage)
        } else {
            itemView.ivImage.gone()
        }

        val username = post.user?.userName ?: ""
        val groupName = post.group?.name ?: ""
        val applyGroupNameSpannable = !groupName.isBlank()  // Only applied if group is available
        val completeUsername = if (applyGroupNameSpannable) {
            itemView.context.getString(R.string.home_label_username_with_group_name, username, groupName)
        } else {
            username
        }

        // First set the complete text
        itemView.tvUserName.setText(completeUsername, TextView.BufferType.SPANNABLE)

        // Set clickable span to the username
        itemView.tvUserName.clickSpannable(spannableText = username,
                textColorRes = R.color.textGray,
                textTypeface = boldTypeface,
                clickListener = userProfileClickListener)

        // If group name is visible then set clickable span to group name
        if (applyGroupNameSpannable) {
            itemView.tvUserName.clickSpannable(spannableText = groupName,
                    textColorRes = R.color.colorPrimary,
                    textTypeface = boldTypeface,
                    clickListener = groupNameClickListener)
        }

        // Add clickable span to all hash tags in the message
        val hashTags = AppUtils.getHashTagsFromString(itemView.tvMessage.text.toString())
        itemView.tvMessage.clickSpannable(spannableTexts = hashTags,
                textColorRes = R.color.colorPrimary,
                clickListener = hashtagClickListener)

        // Add clickable span to all username mentions in the message
        val usernameMentions = AppUtils.getMentionsFromString(itemView.tvMessage.text.toString())
        itemView.tvMessage.clickSpannable(spannableTexts = usernameMentions,
                textColorRes = R.color.colorPrimary,
                clickListener = usernameClickListener)

        updateRepliesAndLikes()
    }

    private fun updateRepliesAndLikes() {
        // Show formatted replies and likes count
        val repliesCount = post.repliesCount ?: 0
        val formattedReplies = itemView.resources.getQuantityString(R.plurals.replies_with_count, repliesCount, repliesCount)

        val likesCount = post.likesCount ?: 0
        val formattedLikes = itemView.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)

        // e.g. "156 Replies · 156 Likes"
        val formattedRepliesAndLikes = String.format("%s · %s", formattedReplies, formattedLikes)
        itemView.tvRepliesLikes.setText(formattedRepliesAndLikes, TextView.BufferType.SPANNABLE)

        itemView.tvRepliesLikes.clickSpannable(spannableText = formattedReplies,
                textColorRes = R.color.textGrayMedium,
                clickListener = postClickListener)

        itemView.tvRepliesLikes.clickSpannable(spannableText = formattedLikes,
                textColorRes = R.color.textGrayMedium,
                clickListener = likesCountClickListener)
    }

    private fun updateLikeButtonState() {
        val isLiked = post.isLiked ?: false
        itemView.ivLike.setImageResource(if (isLiked) {
            R.drawable.ic_heart_selected
        } else {
            R.drawable.ic_heart_normal
        })
    }
}