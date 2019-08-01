package com.pulse.ui.search.groups

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.R
import com.pulse.data.local.UserManager
import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_search.view.*

class SearchGroupViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(adapterPosition, group) }
    }

    private lateinit var group: GroupDto

    fun bind(group: GroupDto) {
        this.group = group
        glide.load(group.imageUrl?.thumbnail)
                .into(itemView.ivProfilePic)
        itemView.tvGroupName.text = group.name

        if (group.isMember == true) {
            itemView.ivParticipationRole.visible()
            // If status is admin, then show user's own image otherwise show a tick which denotes user is a member.
            if (group.participationRole == ApiConstants.PARTICIPATION_ROLE_ADMIN) {
                glide.load(UserManager.getProfile().image?.thumbnail)
                        .into(itemView.ivParticipationRole)
            } else {
                itemView.ivParticipationRole.setImageResource(R.drawable.ic_tick_circle_blue)
            }
        } else {
            itemView.ivParticipationRole.gone()
        }

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
    }

    interface Callback {
        fun onClick(position: Int, group: GroupDto)
    }
}