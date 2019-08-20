package com.checkIt.ui.main.home.viewholders

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.extensions.*
import com.checkIt.ui.groups.GroupPostCallback
import com.checkIt.ui.main.home.PostMediaAdapter
import com.checkIt.ui.videoplayer.VideoPlayerActivity
import com.checkIt.utils.*
import kotlinx.android.synthetic.main.item_home_feed_post.view.*

class HomePostViewHolder(itemView: View,
                         private val glide: GlideRequests,
                         callback: GroupPostCallback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), PostMediaAdapter.Callback {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.roboto_text_bold) }
    private val postClickListener = View.OnClickListener {
        callback.onPostClicked(post, true)
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
        //        post.group?.let { group ->
//            callback.onGroupClicked(group)
//        }
        callback.onPostClicked(post, true)
    }
    private val categoryNameClickListener = View.OnClickListener {
        post.category?.let { category ->
            callback.onGroupCategoryClicked(category)
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

    private var mediaAdapter: PostMediaAdapter = PostMediaAdapter(itemView.context, this)

    override fun playVideo(videoPath: String) {
        VideoPlayerActivity.start(itemView.context, videoPath)
    }

    init {
        itemView.vpMedias.adapter = mediaAdapter
        itemView.setOnClickListener(postClickListener)

        itemView.tvUserName.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!itemView.tvUserName.clickableSpanUnderTouch(event)) {
                    // Forward click to the post click listener if there is no clickable span under touch.
                    postClickListener.onClick(view)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        itemView.tvMessage.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!itemView.tvMessage.clickableSpanUnderTouch(event)) {
                    // Forward click to the post click listener if there is no clickable span under touch.
                    postClickListener.onClick(view)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
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
        itemView.tvTime.text = DateTimeUtils.formatForRecentTime(post.createdOnDateTime)

        val message = post.postText
        if (message.isNullOrBlank()) {
            itemView.tvMessage.gone()
        } else {
            itemView.tvMessage.visible()
            itemView.tvMessage.text = message
        }

        if (!post.locationAddress.isNullOrEmpty()) {
            itemView.tvLocationAddress.visible()
            itemView.tvLocationAddress.text = post.locationName + "," + post.locationAddress
        } else {
            itemView.tvLocationAddress.gone()
        }
        when (post.postType) {
            AppConstants.POST_TYPE_REGULAR -> {
                itemView.ivType.gone()
            }
            AppConstants.POST_TYPE_CONVERSE_NEARBY -> {
                itemView.ivType.visible()
            }
            AppConstants.POST_TYPE_LOOK_NEARBY -> {
                itemView.ivType.visible()
                itemView.ivType.setImageDrawable(itemView.context.getDrawable(R.drawable.binoculars))
            }
        }

        updateLikeButtonState()

        // media pager is visible only if any media is uploaded
        if (post.media.isEmpty()) {
            itemView.vpMedias.gone()
            itemView.indicator.gone()
        } else {
            itemView.vpMedias.visible()
            if (post.media.size > 1)
                itemView.indicator.visible()
            else
                itemView.indicator.gone()
        }
        mediaAdapter.displayImages(post.media)

        val username = post.user?.userName ?: ""
        val groupName = post.group?.name ?: ""
        val categoryName = String.format("(%s)", post.category?.name ?: "")
        val applyGroupNameSpannable = !groupName.isBlank()  // Only applied if group is available
        val completeUsername = if (applyGroupNameSpannable) {
            itemView.context.getString(R.string.home_label_username_with_group_and_category_name, username, groupName, categoryName)
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