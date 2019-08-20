package com.checkIt.ui.groups.groupposts

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.extensions.*
import com.checkIt.ui.custom.SocialEditText
import com.checkIt.ui.groups.PostCallback
import com.checkIt.ui.preview.PreviewActivity
import com.checkIt.utils.AppUtils
import com.checkIt.utils.DateTimeUtils
import com.checkIt.utils.GlideRequests
import com.checkIt.utils.SpannableTextClickListener
import kotlinx.android.synthetic.main.item_group_post.view.*

class GroupPostsAdapter(private val glide: GlideRequests,
                        private val callback: PostCallback) : androidx.recyclerview.widget.RecyclerView.Adapter<GroupPostsAdapter.ViewHolder>() {
    private val posts = mutableListOf<GroupPostDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_group_post), glide, callback)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun displayPosts(posts: List<GroupPostDto>) {
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    fun addPosts(posts: List<GroupPostDto>) {
        val oldListSize = this.posts.size
        this.posts.addAll(posts)
        notifyItemRangeInserted(oldListSize, posts.size)
    }

    fun updatePost(updatedPost: GroupPostDto) {
        val postIndex = posts.indexOfFirst { it.id == updatedPost.id }
        if (postIndex != -1) {
            posts[postIndex] = updatedPost
            notifyItemChanged(postIndex)
        }
    }

    class ViewHolder(itemView: View,
                     private val glide: GlideRequests,
                     private val callback: PostCallback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val postClickListener = View.OnClickListener {
            callback.onPostClicked(post, true)
        }
        private val likesCountClickListener = View.OnClickListener {
            callback.onLikesCountClicked(post)
        }
        private val addCommentClickListener = View.OnClickListener {
            val comment = itemView.etReply.text.toString()
            if (comment.isNotBlank())
                callback.onAddCommentClicked(post, comment)
            itemView.etReply.setText("")
            itemView.etReply.clearFocus()
            itemView.etReply.hideKeyboard()
        }
        private val userProfileClickListener = View.OnClickListener {
            post.user?.let { profile ->
                callback.onUserProfileClicked(profile)
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
            itemView.setOnClickListener(postClickListener)

            itemView.ivImage.setOnClickListener {
                val mediaSize = post.media.size
                if (mediaSize > 1) {
                    PreviewActivity.start(itemView.context, post.media, 0)
                }
            }

            itemView.tvPostCount.setOnClickListener {
                PreviewActivity.start(itemView.context, post.media, 0)
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

            itemView.ivLikePost.setOnClickListener {
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

            itemView.fabSendReply.setOnClickListener(addCommentClickListener)
            itemView.etReply.setTextChangedListener(object : SocialEditText.OnTextChangedListener {
                override fun onTextChanged(text: String) {
                    if (text.isBlank()) {
                        itemView.ivLikePost.visible()
                        itemView.fabSendReply.hide()
                    } else {
                        itemView.ivLikePost.gone()
                        itemView.fabSendReply.show()
                    }
                }
            })
            itemView.etReply.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    itemView.etReply.setSelectAllOnFocus(false)
                    val comment = itemView.etReply.text.toString()
                    if (comment.isNotBlank())
                        callback.onAddCommentClicked(post, comment)
                    itemView.etReply.setText("")
                }
            }
/*
            itemView.ivReply.setOnClickListener {
                callback.onPostClicked(post, true)
            }
*/

            itemView.ivProfile.setOnClickListener(userProfileClickListener)
            itemView.tvUserName.setOnClickListener(userProfileClickListener)
        }

        private lateinit var post: GroupPostDto

        fun bind(post: GroupPostDto) {
            this.post = post

            glide.load(post.user?.image?.thumbnail)
                    .into(itemView.ivProfile)
            itemView.tvUserName.text = post.user?.userName
            itemView.tvTime.text = DateTimeUtils.formatForRecentTime(post.createdOnDateTime)
            itemView.tvMessage.text = post.postText

            updateLikeButtonState()

            // image is shown only if media files are added
            if (post.media.isNotEmpty()) {
                itemView.ivImage.visible()
                val mediaSize = post.media.size
                if (mediaSize > 1) {
                    itemView.tvPostCount.visible()
                    itemView.tvPostCount.text = String.format("+%d", mediaSize - 1)
                } else {
                    itemView.tvPostCount.gone()
                }
                glide.load(post.media.first().original)
                        .into(itemView.ivImage)
            } else {
                itemView.ivImage.gone()
                itemView.tvPostCount.gone()
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

        private fun updateLikeButtonState() {
            val isLiked = post.isLiked ?: false
            itemView.ivLikePost.setImageResource(if (isLiked) {
                R.drawable.ic_heart_selected
            } else {
                R.drawable.ic_heart_normal
            })
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
                    clickListener = postClickListener)
        }
    }

    fun hasFocusRemove(state: Boolean) {

    }
}