package com.conversify.ui.groups.groupposts

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.gone
import com.conversify.extensions.inflate
import com.conversify.extensions.visible
import com.conversify.utils.DateTimeUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_post.view.*

class GroupPostsAdapter(private val glide: GlideRequests,
                        private val callback: Callback) : RecyclerView.Adapter<GroupPostsAdapter.ViewHolder>() {
    private val posts = mutableListOf<GroupPostDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_group_post), glide)
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

    class ViewHolder(itemView: View,
                     private val glide: GlideRequests) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { }

            itemView.ivLike.setOnClickListener { }
        }

        private lateinit var post: GroupPostDto

        fun bind(post: GroupPostDto) {
            this.post = post

            glide.load(post.user?.image?.thumbnail)
                    .into(itemView.ivProfile)
            itemView.tvUserName.text = post.user?.userName
            itemView.tvTime.text = DateTimeUtils.formatChatListingTime(post.createdOnDateTime, itemView.context)
            itemView.tvMessage.text = post.postText

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
    }

    interface Callback
}