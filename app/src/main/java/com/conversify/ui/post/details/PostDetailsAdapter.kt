package com.conversify.ui.post.details

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.LoadingItem
import com.conversify.data.remote.models.post.PostDetailsHeader
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.data.remote.models.post.SubReplyDto
import com.conversify.extensions.inflate
import com.conversify.ui.post.details.viewholders.LoadingViewHolder
import com.conversify.ui.post.details.viewholders.PostDetailsHeaderViewHolder
import com.conversify.ui.post.details.viewholders.PostDetailsReplyViewHolder
import com.conversify.utils.GlideRequests

class PostDetailsAdapter(private val glide: GlideRequests,
                         private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_REPLY = 1
        private const val TYPE_LOADING = 2
    }

    private val items by lazy { mutableListOf<Any>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> PostDetailsHeaderViewHolder(parent.inflate(R.layout.item_post_details_header), glide, callback)
            TYPE_REPLY -> PostDetailsReplyViewHolder(parent.inflate(R.layout.item_post_details_reply), glide, callback)
            TYPE_LOADING -> LoadingViewHolder(parent.inflate(R.layout.item_loading))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PostDetailsHeader -> TYPE_HEADER
            is LoadingItem -> TYPE_LOADING
            else -> TYPE_REPLY
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is PostDetailsHeaderViewHolder -> {
                if (item is PostDetailsHeader) {
                    holder.bind(item.groupPost)
                }
            }
            is PostDetailsReplyViewHolder -> {
                if (item is PostReplyDto) {
                    holder.bind(item)
                }
            }
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<Any>) {
        val oldListSize = this.items.size
        this.items.addAll(items)
        notifyItemRangeInserted(oldListSize, items.size)
    }

    fun addReply(newReply: PostReplyDto) {
        items.add(newReply)
        notifyItemInserted(items.size - 1)
    }

    fun addSubReply(newSubReply: PostReplyDto) {
        val topLevelReplyIndex = items.indexOfFirst { it is PostReplyDto && it.id == newSubReply.parentReplyId }
        if (topLevelReplyIndex != -1) {
            val topLevelReply = items[topLevelReplyIndex] as PostReplyDto
            if (topLevelReply.visibleReplyCount > 0) {
                val newSubReplyIndex = topLevelReplyIndex + topLevelReply.visibleReplyCount
                items.add(newSubReplyIndex, newSubReply)
                notifyItemInserted(newSubReplyIndex)
            }
            notifyItemChanged(topLevelReplyIndex)
        }
    }

    fun getReply(replyId: String): PostReplyDto? {
        return items.firstOrNull { it is PostReplyDto && it.id == replyId } as? PostReplyDto
    }

    fun setLoading(visible: Boolean) {
        if (visible) {
            items.add(LoadingItem)
            notifyItemInserted(items.size - 1)
        } else {
            if (items.lastOrNull() is LoadingItem) {
                val loadingIndex = items.size - 1
                items.removeAt(loadingIndex)
                notifyItemRemoved(loadingIndex)
            }
        }
    }

    fun displaySubReplies(subReply: SubReplyDto) {
        val parentReplyIndex = items.indexOfFirst {
            it is PostReplyDto && it.id == subReply.parentReply.id
        }

        if (parentReplyIndex != -1) {
            notifyItemChanged(parentReplyIndex)
            items.addAll(parentReplyIndex + 1, subReply.replies)
            notifyItemRangeInserted(parentReplyIndex + 1, subReply.replies.size)
        }
    }

    fun notifyParentReplyChange(parentReply: PostReplyDto) {
        val parentReplyIndex = items.indexOfFirst {
            it is PostReplyDto && it.id == parentReply.id
        }
        if (parentReplyIndex != -1) {
            notifyItemChanged(parentReplyIndex)
        }
    }

    fun hideAllSubReplies(parentReply: PostReplyDto) {
        val parentReplyIndex = items.indexOfFirst {
            it is PostReplyDto && it.id == parentReply.id
        }
        if (parentReplyIndex != -1) {
            val subRepliesStartIndex = parentReplyIndex + 1
            val subRepliesCount = parentReply.subReplies.size

            for (subReplyIndex in 0 until subRepliesCount) {
                items.removeAt(subRepliesStartIndex)
            }

            parentReply.visibleReplyCount = 0
            notifyItemChanged(parentReplyIndex)
            notifyItemRangeRemoved(subRepliesStartIndex, subRepliesCount)
        }
    }

    fun showAllSubReplies(parentReply: PostReplyDto) {
        val parentReplyIndex = items.indexOfFirst {
            it is PostReplyDto && it.id == parentReply.id
        }
        if (parentReplyIndex != -1) {
            val subRepliesStartIndex = parentReplyIndex + 1
            val subRepliesCount = parentReply.subReplies.size

            items.addAll(subRepliesStartIndex, parentReply.subReplies)
            notifyItemRangeInserted(subRepliesStartIndex, subRepliesCount)

            parentReply.visibleReplyCount = subRepliesCount
            notifyItemChanged(parentReplyIndex)
        }
    }

    fun notifyHeaderChanged() {
        if (items.firstOrNull() is PostDetailsHeader) {
            notifyItemChanged(0)
        }
    }

    interface Callback : PostDetailsHeaderViewHolder.Callback, PostDetailsReplyViewHolder.Callback
}