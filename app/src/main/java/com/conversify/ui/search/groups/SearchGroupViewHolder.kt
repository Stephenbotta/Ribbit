package com.conversify.ui.search.groups

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_search.view.*


class SearchGroupViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(group) }
    }

    private lateinit var group: GroupDto

    fun bind(group: GroupDto) {
        this.group = group
        glide.load(group.imageUrl?.thumbnail)
                .into(itemView.ivProfilePic)
        itemView.tvGroupName.text = group.name
    }

    interface Callback {
        fun onClick(group: GroupDto)
    }
}