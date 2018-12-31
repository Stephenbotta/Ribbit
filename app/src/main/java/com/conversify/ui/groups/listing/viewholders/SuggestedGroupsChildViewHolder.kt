package com.conversify.ui.groups.listing.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_suggested_groups_child.view.*

class SuggestedGroupsChildViewHolder(itemView: View,
                                     private val glide: GlideRequests,
                                     private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onSuggestedGroupClicked(group)
            }
        }

        itemView.ivRemove.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onRemoveSuggestedGroupClicked(group)
            }
        }
    }

    private lateinit var group: GroupDto

    fun bind(group: GroupDto) {
        this.group = group

        glide.load(group.imageUrl?.original)
                .into(itemView.ivGroup)
        itemView.tvGroupName.text = group.name

        val members = group.memberCount ?: 0
        itemView.tvMemberCount.text = itemView.context.resources
                .getQuantityString(R.plurals.members_with_count, members, members)

        if (group.isPrivate == true) {
            itemView.ivPrivate.visible()
        } else {
            itemView.ivPrivate.gone()
        }

        // Only visible when request is pending or rejected
        when (group.requestStatus) {
            ApiConstants.REQUEST_STATUS_PENDING -> {
                itemView.tvRequestStatus.visible()
                itemView.tvRequestStatus.setText(R.string.venues_label_pending)
            }

            ApiConstants.REQUEST_STATUS_REJECTED -> {
                itemView.tvRequestStatus.visible()
                itemView.tvRequestStatus.setText(R.string.venues_label_rejected)
            }

            else -> {
                itemView.tvRequestStatus.gone()
            }
        }

        itemView.ivFavourite.setImageResource(if (group.isMember == true) {
            R.drawable.ic_star_selected
        } else {
            R.drawable.ic_star_normal
        })
    }

    interface Callback {
        fun onSuggestedGroupClicked(group: GroupDto)
        fun onRemoveSuggestedGroupClicked(group: GroupDto)
    }
}