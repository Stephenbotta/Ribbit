package com.ribbit.ui.search.groups

import android.view.View
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_search.view.*

class SearchGroupViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(group) }
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
        fun onClick(group: GroupDto)
    }
}