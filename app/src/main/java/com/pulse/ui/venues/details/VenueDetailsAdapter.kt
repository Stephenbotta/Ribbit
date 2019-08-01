package com.pulse.ui.venues.details

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.chat.MemberDto
import com.pulse.data.remote.models.groups.AddParticipantsDto
import com.pulse.data.remote.models.venues.VenueDto
import com.pulse.extensions.inflate
import com.pulse.ui.creategroup.create.viewholders.AddParticipantsViewHolder
import com.pulse.ui.venues.details.viewholder.VenueDetailsExitGroupViewHolder
import com.pulse.ui.venues.details.viewholder.VenueDetailsHeaderViewHolder
import com.pulse.ui.venues.details.viewholder.VenueDetailsMemberViewHolder
import com.pulse.utils.GlideRequests

class VenueDetailsAdapter(val glide: GlideRequests,
                          private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ADD_PARTICIPANTS = 1
        private const val VIEW_TYPE_MEMBER = 2
        private const val VIEW_TYPE_EXIT_GROUP = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> VenueDetailsHeaderViewHolder(parent.inflate(R.layout.item_venue_details_header), callback)

            VIEW_TYPE_ADD_PARTICIPANTS -> AddParticipantsViewHolder(parent.inflate(R.layout.item_group_add_participants), callback)

            VIEW_TYPE_MEMBER -> VenueDetailsMemberViewHolder(parent.inflate(R.layout.item_venue_details_member), glide, callback)

            VIEW_TYPE_EXIT_GROUP -> VenueDetailsExitGroupViewHolder(parent.inflate(R.layout.item_venue_details_exit_group), callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is VenueDetailsHeaderViewHolder -> {
                if (item is VenueDto) {
                    holder.bind(item)
                }
            }

            is VenueDetailsMemberViewHolder -> {
                if (item is MemberDto) {
                    holder.bind(item)
                }
            }
            is VenueDetailsExitGroupViewHolder -> {
                if (item is String) {
                    holder.bind(item)
                }
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is VenueDto -> VIEW_TYPE_HEADER
            is AddParticipantsDto -> VIEW_TYPE_ADD_PARTICIPANTS
            is MemberDto -> VIEW_TYPE_MEMBER
            else -> VIEW_TYPE_EXIT_GROUP
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun updateHeader() {
        if (items.firstOrNull() is VenueDto) {
            notifyItemChanged(0)
        }
    }

    interface Callback : VenueDetailsHeaderViewHolder.Callback,
            AddParticipantsViewHolder.Callback,
            VenueDetailsMemberViewHolder.Callback,
            VenueDetailsExitGroupViewHolder.Callback
}