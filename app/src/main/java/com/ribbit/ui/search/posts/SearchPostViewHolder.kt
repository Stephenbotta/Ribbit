package com.ribbit.ui.search.posts

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_post_search.view.*

class SearchPostViewHolder(itemView: View, private val glide: GlideRequests, private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(post) }
    }

    private lateinit var post: GroupPostDto

    fun bind(post: GroupPostDto) {
        this.post = post
        glide.load(post.media.first().original)
                .into(itemView.ivThumbnail)
        if (post.type.equals(ApiConstants.GROUP_POST_TYPE_VIDEO)) {
            itemView.ivPlay.visible()
        }
    }


    interface Callback {
        fun onClick(post: GroupPostDto)
    }
}