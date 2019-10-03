package com.ribbit.ui.venues.details.viewholder

import android.view.View
import com.ribbit.data.remote.models.chat.MemberDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideRequests
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