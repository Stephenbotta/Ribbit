package com.checkIt.ui.groups.details

import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.chat.MemberDto
import com.checkIt.data.remote.models.groups.AddParticipantsDto
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.extensions.inflate
import com.checkIt.ui.creategroup.create.viewholders.AddParticipantsViewHolder
import com.checkIt.ui.groups.details.viewholder.GroupDetailsExitGroupViewHolder
import com.checkIt.ui.groups.details.viewholder.GroupDetailsHeaderViewHolder
import com.checkIt.ui.groups.details.viewholder.GroupDetailsMemberViewHolder
import com.checkIt.utils.GlideRequests

class GroupDetailsAdapter(val glide: GlideRequests,
                          private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ADD_PARTICIPANTS = 1
        private const val VIEW_TYPE_MEMBER = 2
        private const val VIEW_TYPE_EXIT_GROUP = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> GroupDetailsHeaderViewHolder(parent.inflate(R.layout.item_group_details_header), callback)

            VIEW_TYPE_ADD_PARTICIPANTS -> AddParticipantsViewHolder(parent.inflate(R.layout.item_group_add_participants), callback)

            VIEW_TYPE_MEMBER -> GroupDetailsMemberViewHolder(parent.inflate(R.layout.item_venue_details_member), glide, callback)

            VIEW_TYPE_EXIT_GROUP -> GroupDetailsExitGroupViewHolder(parent.inflate(R.layout.item_venue_details_exit_group), callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is GroupDetailsHeaderViewHolder -> {
                if (item is GroupDto) {
                    holder.bind(item)
                }
            }
            is GroupDetailsMemberViewHolder -> {
                if (item is MemberDto) {
                    holder.bind(item)
                }
            }
            is GroupDetailsExitGroupViewHolder -> {
                if (item is String) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is GroupDto -> VIEW_TYPE_HEADER
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
        if (items.firstOrNull() is GroupDto) {
            notifyItemChanged(0)
        }
    }

    interface Callback : GroupDetailsHeaderViewHolder.Callback,
            AddParticipantsViewHolder.Callback,
            GroupDetailsMemberViewHolder.Callback,
            GroupDetailsExitGroupViewHolder.Callback
}