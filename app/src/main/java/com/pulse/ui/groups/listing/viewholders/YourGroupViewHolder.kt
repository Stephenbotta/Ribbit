package com.pulse.ui.groups.listing.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.R
import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.utils.GlideRequests
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

    fun bind(group: GroupDto, ownProfile: ProfileDto) {
        this.group = group

        glide.load(group.imageUrl?.thumbnail)
                .into(itemView.ivGroup)
        itemView.tvGroupName.text = group.name

        if (group.isMember == true) {
            itemView.ivParticipationRole.visible()

            // If status is admin, then show user's own image otherwise show a tick which denotes user is a member.
            if (group.participationRole == ApiConstants.PARTICIPATION_ROLE_ADMIN) {
                glide.load(ownProfile.image?.thumbnail)
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

        val unreadCount = group.unreadCount ?: 0
        if (unreadCount == 0) {
            itemView.tvUnreadCount.gone()
        } else {
            itemView.tvUnreadCount.text = unreadCount.toString()
            itemView.tvUnreadCount.visible()
        }

        val memberCount = group.memberCount ?: 0
        itemView.tvMemberCount.text = itemView.context.resources.getQuantityString(R.plurals.members_with_count, memberCount, memberCount)
    }

    interface Callback {
        fun onYourGroupClicked(group: GroupDto)
    }
}