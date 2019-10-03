package com.ribbit.ui.groups.listing.viewholders

import android.view.View
import com.ribbit.R
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_groups_your_group.view.*

class YourGroupViewHolder(itemView: View,
                          private val glide: GlideRequests,
                          private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener {
            if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
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