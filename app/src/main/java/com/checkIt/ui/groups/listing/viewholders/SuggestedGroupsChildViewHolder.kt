package com.checkIt.ui.groups.listing.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.R
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.visible
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_suggested_groups_child.view.*

class SuggestedGroupsChildViewHolder(itemView: View,
                                     private val glide: GlideRequests,
                                     private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener {
            if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                callback.onSuggestedGroupClicked(group)
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
                itemView.tvRequestStatus.gone()
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
    }
}