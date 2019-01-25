package com.conversify.ui.search.posts

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_post_search.view.*

class SearchPostViewHolder(itemView: View,
                           private val glide: GlideRequests,
                           private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(adapterPosition, post) }
    }

    private lateinit var post: GroupPostDto

    fun bind(post: GroupPostDto) {
        this.post = post
        glide.load(post.imageUrl?.original)
                .into(itemView.ivThumbnail)
        if (post.type.equals(ApiConstants.GROUP_POST_TYPE_VIDEO)) {
            itemView.ivPlay.visible()
        }
    }


    interface Callback {
        fun onClick(position: Int, post: GroupPostDto)
    }
}