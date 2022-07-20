package com.ribbit.ui.main.home.viewholders

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.extensions.*
import com.ribbit.ui.groups.GroupPostCallback
import com.ribbit.utils.*
import kotlinx.android.synthetic.main.item_home_feed_post.view.*

class HomePostViewHolder(itemView: View, private val glide: GlideRequests,
                         private val callback: GroupPostCallback) : RecyclerView.ViewHolder(itemView)/*, PostMediaAdapter.Callback*/ {
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

    /*private var mediaAdapter: PostMediaAdapter = PostMediaAdapter(itemView.context, this)*/

    /*override fun openMediaDetail(media: ImageUrlDto) {
        callback.onPostMediaClicked(post, true, media)
    }*/

    init {
        /*itemView.vpMedias.adapter = mediaAdapter*/
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

        itemView.ivMedia1.setOnClickListener {
            callback.onPostMediaClicked(post, true, post.media[0])
        }

        itemView.ivMedia2.setOnClickListener {
            callback.onPostMediaClicked(post, true, post.media[1])
        }

        itemView.ivMedia3.setOnClickListener {
            callback.onPostMediaClicked(post, true, post.media[2])
        }

        itemView.ivMedia4.setOnClickListener {
            callback.onPostMediaClicked(post, true, post.media[3])
        }
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
            itemView.tvLocationAddress.text = post.locationName /*String.format("%s , %s", post.locationName, post.locationAddress)*/
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

        if (post.media.isEmpty()) {
            itemView.clMedia.gone()
            /*itemView.vpMedias.gone()
            itemView.indicator.gone()*/
        } else {
            itemView.clMedia.visible()
            /*itemView.vpMedias.visible()
            if (post.media.size > 1)
                itemView.indicator.visible()
            else
                itemView.indicator.gone()*/

            when (post.media.size) {
                2 -> {
                    itemView.ivMedia1.visible()
                    itemView.ivMedia2.visible()
                    itemView.ivMedia3.gone()
                    itemView.ivMedia4.gone()

                    loadMedia(itemView.ivMedia1, post.media[0], itemView.ivPlay1, itemView.ivMostLiked1)
                    loadMedia(itemView.ivMedia2, post.media[1], itemView.ivPlay2, itemView.ivMostLiked2)
                }
                3 -> {
                    itemView.ivMedia1.visible()
                    itemView.ivMedia2.visible()
                    itemView.ivMedia3.visible()
                    itemView.ivMedia4.gone()

                    loadMedia(itemView.ivMedia1, post.media[0], itemView.ivPlay1, itemView.ivMostLiked1)
                    loadMedia(itemView.ivMedia2, post.media[1], itemView.ivPlay2, itemView.ivMostLiked2)
                    loadMedia(itemView.ivMedia3, post.media[2], itemView.ivPlay3, itemView.ivMostLiked3)
                }
                4 -> {
                    itemView.ivMedia1.visible()
                    itemView.ivMedia2.visible()
                    itemView.ivMedia3.visible()
                    itemView.ivMedia4.visible()

                    loadMedia(itemView.ivMedia1, post.media[0], itemView.ivPlay1, itemView.ivMostLiked1)
                    loadMedia(itemView.ivMedia2, post.media[1], itemView.ivPlay2, itemView.ivMostLiked2)
                    loadMedia(itemView.ivMedia3, post.media[2], itemView.ivPlay3, itemView.ivMostLiked3)
                    loadMedia(itemView.ivMedia4, post.media[3], itemView.ivPlay4, itemView.ivMostLiked4)
                }
                else -> {
                    itemView.ivMedia1.visible()
                    itemView.ivMedia2.gone()
                    itemView.ivMedia3.gone()
                    itemView.ivMedia4.gone()

                    loadMedia(itemView.ivMedia1, post.media[0], itemView.ivPlay1, itemView.ivMostLiked1)
                }
            }
        }
        /*mediaAdapter.displayImages(post.media)*/

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

    private fun loadMedia(ivMedia: ImageView, imageUrl: ImageUrlDto, ivPlay: ImageView, ivMostLiked: ImageView) {
        glide.load(imageUrl.thumbnail)
                .into(ivMedia)

        /*itemView.ivPlay1.gone()
        itemView.ivPlay2.gone()
        itemView.ivPlay3.gone()
        itemView.ivPlay4.gone()*/

        when (imageUrl.mediaType) {
            ApiConstants.POST_TYPE_VIDEO -> ivPlay.visible()
            else -> ivPlay.gone()
        }

        /*if (imageUrl.isMostLiked == true)
            ivMostLiked.visible()
        else
            ivMostLiked.gone()*/
    }

    private fun updateRepliesAndLikes() {
        // Show formatted replies and likes count
        val repliesCount = post.repliesCount ?: 0
        val likesCount = post.likesCount ?: 0
        val formattedReplies = itemView.resources.getQuantityString(R.plurals.replies_with_count, repliesCount, repliesCount)

        /*var totalLikes = 0.0
        post.media.forEach { totalLikes += it.likesCount ?: 0 }
        val maxLikeCount = post.media.map { it.likesCount ?: 0 }.max() ?: 0
        val likesCount = if (totalLikes > 0) {
            (maxLikeCount / totalLikes) * 100
        } else {
            0.0
        }*/
        val formattedLikes = itemView.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)

        // e.g. "156 Replies · 156 Likes"
        val formattedRepliesAndLikes = if (likesCount > 0.0) {
            String.format("%s · %s", formattedReplies, formattedLikes)
        } else {
            String.format("%s", formattedReplies)
        }

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