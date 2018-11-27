package com.conversify.ui.venues.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue_details_member.view.*

class VenueDetailsMemberViewHolder(itemView: View,
                                   private val glide: GlideRequests,
                                   private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onMemberClicked(member) }
    }

    private lateinit var member: VenueMemberDto

    fun bind(member: VenueMemberDto) {
        this.member = member

        if (member.isAdmin == true) {
            itemView.tvLabelAdmin.visible()
        } else {
            itemView.tvLabelAdmin.gone()
        }

        glide.load(member.user?.image?.thumbnail)
                .into(itemView.ivProfile)
        itemView.tvUserName.text = member.user?.userName
    }

    interface Callback {
        fun onMemberClicked(member: VenueMemberDto)
    }
}