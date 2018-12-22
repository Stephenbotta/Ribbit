package com.conversify.ui.post.details.viewholders

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.extensions.clickSpannable
import com.conversify.utils.AppUtils
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import com.conversify.utils.SpannableTextClickListener
import kotlinx.android.synthetic.main.item_post_details_reply.view.*

class PostDetailsReplyViewHolder(itemView: View,
                                 private val glide: GlideRequests,
                                 private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val boldTypeface by lazy { ResourcesCompat.getFont(itemView.context, R.font.brandon_text_bold) }
    private val userProfileClickListener = View.OnClickListener {

    }
    private val hashtagClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
        }
    }
    private val usernameClickListener = object : SpannableTextClickListener {
        override fun onSpannableTextClicked(text: String, view: View) {
        }
    }

    init {
        itemView.ivProfile.setOnClickListener(userProfileClickListener)

        itemView.tvLikes.setOnClickListener {
            callback.onLikesCountClicked(reply)
        }

        itemView.btnReply.setOnClickListener {
            callback.onReplyClicked(reply)
        }

        itemView.ivLike.setOnClickListener { }
    }

    private lateinit var reply: PostReplyDto

    fun bind(reply: PostReplyDto) {
        this.reply = reply

        val profile = reply.commentBy ?: reply.replyBy
        glide.load(profile?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvTime.text = DateTimeUtils.formatChatListingTime(reply.createdOnDateTime, itemView.context)

        val likesCount = reply.likeCount ?: 0
        itemView.tvLikes.text = itemView.resources.getQuantityString(R.plurals.likes_with_count, likesCount, likesCount)

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

    interface Callback {
        fun onLikesCountClicked(reply: PostReplyDto)
        fun onReplyClicked(reply: PostReplyDto)
    }
}