package com.ribbit.ui.post.details.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.clickSpannable
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.ui.images.ImagesActivity
import com.ribbit.ui.videoplayer.VideoPlayerActivity
import com.ribbit.utils.AppUtils
import com.ribbit.utils.DateTimeUtils
import com.ribbit.utils.GlideRequests
import com.ribbit.utils.SpannableTextClickListener
import kotlinx.android.synthetic.main.item_post_details_header.view.*

class PostDetailsHeaderViewHolder(itemView: View, private val glide: GlideRequests, private val media: ImageUrlDto?,
                                  private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.roboto_text_bold) }
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
            callback.onHashTagClicked(text)
        }
    }
    private val usernameClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
            callback.onUsernameMentionClicked(text)
        }
    }
    private val categoryNameClickListener = View.OnClickListener {
        post.category?.let { category ->
            callback.onGroupCategoryClicked(category)
        }
    }

    init {
        itemView.ivImage.setOnClickListener {
            when (media?.mediaType) {
                ApiConstants.POST_TYPE_VIDEO -> {
                    VideoPlayerActivity.start(it.context, media.videoUrl ?: "")
                }
                else -> {
                    ImagesActivity.start(it.context, arrayListOf(media?.original ?: ""))
                }
            }

        }
    }

    private lateinit var post: GroupPostDto

    fun bind(post: GroupPostDto) {
        this.post = post

        glide.load(post.user?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatForRecentTime(post.createdOnDateTime)
        if (post.postText.isNullOrBlank()) {
            itemView.tvMessage.gone()
        } else {
            itemView.tvMessage.visible()
            itemView.tvMessage.text = post.postText
        }

        /*// Image is only visible when post type is image
        if (post.type == ApiConstants.GROUP_POST_TYPE_IMAGE) {*/
        if (media != null) {
            itemView.ivImage.visible()
            glide.load(media.original)
                    .into(itemView.ivImage)
            /*if (media.isMostLiked == true)
                itemView.ivLiked.visible()
            else
                itemView.ivLiked.gone()*/
            when (media.mediaType) {
                ApiConstants.POST_TYPE_VIDEO -> itemView.ivVideo.visible()
                else -> itemView.ivVideo.gone()
            }
        } else {
            itemView.ivImage.gone()
            itemView.ivVideo.gone()
            /*itemView.ivLiked.gone()*/
        }
        /*} else {
            itemView.ivImage.gone()
        }*/

        val username = post.user?.userName ?: ""
        val groupName = post.group?.name ?: ""
        val categoryName = String.format("(%s)", post.category?.name ?: "")
        val applyGroupNameSpannable = !groupName.isBlank()  // Only applied if group is available
        val completeText = if (applyGroupNameSpannable) {
            itemView.context.getString(R.string.home_label_username_with_group_and_category_name, username, groupName, categoryName)
        } else {
            username
        }

        // First set the complete text
        itemView.tvUserName.setText(completeText, TextView.BufferType.SPANNABLE)

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

            itemView.tvUserName.clickSpannable(spannableText = categoryName,
                    textColorRes = R.color.colorPrimary,
                    textTypeface = boldTypeface,
                    clickListener = categoryNameClickListener)
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

        // Show formatted replies and likes count
        val repliesCount = post.repliesCount ?: 0
        val formattedReplies = itemView.resources.getQuantityString(R.plurals.replies_with_count, repliesCount, repliesCount)

        val likesCount = post.likesCount ?: 0
        val formattedLikes = itemView.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)

        // e.g. "156 Replies · 156 Likes"
        val formattedRepliesAndLikes = String.format("%s · %s", formattedReplies, formattedLikes)
        itemView.tvRepliesLikes.setText(formattedRepliesAndLikes, TextView.BufferType.SPANNABLE)

        itemView.tvRepliesLikes.clickSpannable(spannableText = formattedLikes,
                textColorRes = R.color.textGrayMedium,
                clickListener = likesCountClickListener)
    }

    interface Callback {
        fun onLikesCountClicked(post: GroupPostDto)
        fun onGroupClicked(group: GroupDto)
        fun onUserProfileClicked(profile: ProfileDto)
        fun onHashTagClicked(tag: String)
        fun onUsernameMentionClicked(username: String)
        fun onGroupCategoryClicked(category: InterestDto)
    }
}