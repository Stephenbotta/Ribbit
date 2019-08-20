package com.checkIt.ui.venues.details.viewholder

import android.view.View
import com.checkIt.data.remote.models.chat.MemberDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.visible
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue_details_member.view.*

class VenueDetailsMemberViewHolder(itemView: View,
                                   private val glide: GlideRequests,
                                   private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onMemberClicked(member) }
    }

    private lateinit var member: MemberDto

    fun bind(member: MemberDto) {
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
        fun onMemberClicked(member: MemberDto)
    }
}