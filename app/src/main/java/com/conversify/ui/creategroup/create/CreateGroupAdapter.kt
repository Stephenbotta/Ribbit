package com.conversify.ui.creategroup.create

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.data.remote.models.groups.CreateGroupHeaderDto
import com.conversify.data.remote.models.groups.GroupAddParticipantsDto
import com.conversify.extensions.inflate
import com.conversify.ui.creategroup.create.viewholders.CreateGroupHeaderViewHolder
import com.conversify.ui.venues.details.viewholder.VenueDetailsMemberViewHolder
import com.conversify.utils.GlideRequests

class CreateGroupAdapter(val glide: GlideRequests,
                         private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ADD_PARTICIPANTS = 1
        private const val VIEW_TYPE_MEMBER = 2
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> CreateGroupHeaderViewHolder(parent.inflate(R.layout.item_venue_details_header), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is CreateGroupHeaderViewHolder -> {
                if (item is CreateGroupHeaderDto) {
                    holder.bind(item)
                }
            }

            is VenueDetailsMemberViewHolder -> {
                if (item is VenueMemberDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is CreateGroupHeaderDto -> VIEW_TYPE_HEADER
            is GroupAddParticipantsDto -> VIEW_TYPE_ADD_PARTICIPANTS
            else -> VIEW_TYPE_MEMBER
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun updateHeader() {
        if (items.firstOrNull() is CreateGroupHeaderDto) {
            notifyItemChanged(0)
        }
    }

    interface Callback : CreateGroupHeaderViewHolder.Callback
}