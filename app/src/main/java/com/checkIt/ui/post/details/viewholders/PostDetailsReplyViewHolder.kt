package com.checkIt.ui.post.details.viewholders

import android.content.Intent
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.local.PrefsManager
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.people.UserCrossedDto
import com.checkIt.data.remote.models.post.PostReplyDto
import com.checkIt.extensions.*
import com.checkIt.ui.people.details.PeopleDetailsActivity
import com.checkIt.ui.profile.ProfileActivity
import com.checkIt.utils.*
import kotlinx.android.synthetic.main.item_post_details_reply.view.*
import timber.log.Timber

class PostDetailsReplyViewHolder(itemView: View,
                                 private val glide: GlideRequests,
                                 private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val topLevelReplyStartMargin by lazy { itemView.context.pxFromDp(16) }
    private val subLevelReplyStartMargin by lazy { itemView.context.pxFromDp(56) }
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.roboto_text_bold) }

    private lateinit var profile: ProfileDto

    private val userProfileClickListener = View.OnClickListener {
        val data = UserCrossedDto()
        data.profile = profile
        PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, profile.id ?: "")
        if (profile.id == UserManager.getUserId()) {
            itemView.context.startActivity(Intent(itemView.context, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(itemView.context, data, AppConstants.REQ_CODE_BLOCK_USER)
            itemView.context.startActivity(intent)
        }

    }
    private val hashtagClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
        }
    }
    private val usernameClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
            val data = UserCrossedDto()
            if (profile.userName == text.removeRange(0, 1)) {
                itemView.context.startActivity(Intent(itemView.context, ProfileActivity::class.java))
            } else {
                data.profile = reply.replyBy
                PrefsManager.get().save(PrefsManager.PREF_PEOPLE_USER_ID, reply.parentReplyOwnerId
                        ?: "")
                val intent = PeopleDetailsActivity.getStartIntent(itemView.context, data, AppConstants.REQ_CODE_BLOCK_USER)
                itemView.context.startActivity(intent)
            }

        }
    }

    init {
        itemView.ivProfile.setOnClickListener(userProfileClickListener)

        itemView.tvLikes.setOnClickListener {
            if (isValidPosition()) {
                callback.onLikesCountClicked(reply)
            }
        }

        itemView.btnReply.setOnClickListener {
            if (isValidPosition()) {
                callback.onReplyClicked(reply, isTopLevelReply())
            }
        }

        itemView.ivLike.setOnClickListener {
            if (isValidPosition() && it.context.isNetworkActive()) {
                val isLiked = !(reply.liked ?: false)     // toggle liked state
                reply.liked = isLiked

                val currentLikesCount = reply.likesCount ?: 0
                reply.likesCount = if (isLiked) {
                    currentLikesCount + 1
                } else {
                    currentLikesCount - 1
                }
                updateLikesCount()
                updateLikeButtonState()
                callback.onLikeReplyClicked(reply, isLiked, isTopLevelReply())
            }
        }

        itemView.btnLoadReplies.setOnClickListener {
            if (!isValidPosition()) return@setOnClickListener

            // Ignore click if sub-replies are currently loading
            if (reply.subRepliesLoading) {
                Timber.d("Load replies clicked. Sub-replies are already loading.")
                return@setOnClickListener
            }

            // If pending reply count is 0, then we already have all replies available
            if (reply.pendingReplyCount == 0) {
                // If total reply count is equal to the replies that are currently visible, then hide all replies
                if ((reply.replyCount ?: 0) == reply.visibleReplyCount) {
                    callback.onHideAllRepliesClicked(reply)
                } else if (reply.visibleReplyCount == 0) {
                    // If non of the replies are visible, then clicking will show all replies.
                    callback.onShowAllRepliesClicked(reply)
                }
            } else {
                // If pending reply count is non-zero, then we have to get pending replies.
                callback.onLoadRepliesClicked(reply)
            }
            Timber.i("Load replies clicked, pending replies : ${reply.pendingReplyCount}")
        }

        itemView.setOnLongClickListener {
            callback.onLongPress(reply)
            true
        }
    }

    private lateinit var reply: PostReplyDto

    fun bind(reply: PostReplyDto) {
        this.reply = reply

        // Set the start margin of profile image based on its level
        val profileImageParams = (itemView.ivProfile.layoutParams as ViewGroup.MarginLayoutParams)
        profileImageParams.marginStart = if (isTopLevelReply()) {
            topLevelReplyStartMargin
        } else {
            subLevelReplyStartMargin
        }

        val profile = reply.commentBy ?: reply.replyBy
        glide.load(profile?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatForRecentTime(reply.createdOnDateTime)
        this.profile = profile ?: ProfileDto()

        updateLikesCount()
        updateLikeButtonState()

        // Load replies is only visible when reply count is more than 0
        val replyCount = reply.replyCount ?: 0
        if (replyCount == 0) {
            itemView.btnLoadReplies.gone()
        } else {
            itemView.btnLoadReplies.visible()

            val pendingReplyCount = replyCount - reply.visibleReplyCount
            if (pendingReplyCount == 0) {
                itemView.btnLoadReplies.setText(R.string.post_details_label_hide_all_replies)
            } else {
                itemView.btnLoadReplies.text = itemView.context.getString(R.string.post_details_btn_load_replies_with_count, pendingReplyCount)
            }
        }

        // Set the sub replies loading state
        if (reply.subRepliesLoading) {
            itemView.loadRepliesLoading.visible()
        } else {
            itemView.loadRepliesLoading.gone()
        }

        // Set formatted username and message
        val username = profile?.userName ?: ""
        val message = reply.comment ?: reply.reply
        itemView.tvMessage.text = String.format("%s %s", username, message)

        // Add clickable span to the username in the message
        itemView.tvMessage.clickSpannable(spannableText = username,
                textColorRes = R.color.textGray,
                textTypeface = boldTypeface,
                clickListener = userProfileClickListener)

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
    }

    private fun updateLikesCount() {
        val likesCount = reply.likesCount ?: 0
        itemView.tvLikes.text = itemView.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)
    }

    private fun updateLikeButtonState() {
        val isLiked = reply.liked ?: false
        itemView.ivLike.setImageResource(if (isLiked) {
            R.drawable.ic_heart_selected
        } else {
            R.drawable.ic_heart_normal
        })
    }

    /**
     * "commentBy" is available only for top-level replies. "replyBy" is available for sub-replies.
     * */
    private fun isTopLevelReply() = reply.commentBy != null

    interface Callback {
        fun onLikesCountClicked(reply: PostReplyDto)
        fun onReplyClicked(reply: PostReplyDto, isTopLevelReply: Boolean)
        fun onLoadRepliesClicked(parentReply: PostReplyDto)
        fun onShowAllRepliesClicked(parentReply: PostReplyDto)
        fun onHideAllRepliesClicked(parentReply: PostReplyDto)
        fun onLikeReplyClicked(reply: PostReplyDto, isLiked: Boolean, topLevelReply: Boolean)
        fun onLongPress(parentReply: PostReplyDto)
    }
}