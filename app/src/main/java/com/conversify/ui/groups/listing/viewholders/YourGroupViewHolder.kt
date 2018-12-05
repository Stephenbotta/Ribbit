package com.conversify.ui.groups.listing.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_groups_your_group.view.*

class YourGroupViewHolder(itemView: View,
                          private val glide: GlideRequests,
                          private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onYourGroupClicked(group)
            }
        }
    }

    private lateinit var group: GroupDto

    fun bind(group: GroupDto) {
        this.group = group

        glide.load(group.imageUrl?.thumbnail)
                .into(itemView.ivGroup)
        itemView.tvGroupName.text = group.name

        val unreadCount = group.unreadCount ?: 0
        if (unreadCount == 0) {
            itemView.tvUnreadCount.gone()
        } else {
            itemView.tvUnreadCount.text = unreadCount.toString()
            itemView.tvUnreadCount.visible()
        }
    }

    interface Callback {
        fun onYourGroupClicked(group: GroupDto)
    }
}