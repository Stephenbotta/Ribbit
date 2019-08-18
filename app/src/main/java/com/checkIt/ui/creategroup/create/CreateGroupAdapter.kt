package com.checkIt.ui.creategroup.create

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.groups.AddParticipantsDto
import com.checkIt.data.remote.models.groups.CreateGroupHeaderDto
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.extensions.inflate
import com.checkIt.ui.creategroup.addparticipants.ParticipantViewHolder
import com.checkIt.ui.creategroup.create.viewholders.AddParticipantsViewHolder
import com.checkIt.ui.creategroup.create.viewholders.CreateGroupHeaderViewHolder
import com.checkIt.utils.GlideRequests

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
            VIEW_TYPE_HEADER -> CreateGroupHeaderViewHolder(parent.inflate(R.layout.item_create_group_header), glide, callback)
            VIEW_TYPE_ADD_PARTICIPANTS -> AddParticipantsViewHolder(parent.inflate(R.layout.item_group_add_participants), callback)
            VIEW_TYPE_MEMBER -> ParticipantViewHolder(parent.inflate(R.layout.item_group_participant), glide, false)
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

            is ParticipantViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is CreateGroupHeaderDto -> VIEW_TYPE_HEADER
            is AddParticipantsDto -> VIEW_TYPE_ADD_PARTICIPANTS
            is ProfileDto-> VIEW_TYPE_MEMBER
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

    fun displayMembers(members: List<ProfileDto>) {
        // Remove all existing members before adding/updating new ones
        this.items.removeAll { it is ProfileDto }
        this.items.addAll(members)
        notifyDataSetChanged()
    }

    interface Callback : CreateGroupHeaderViewHolder.Callback, AddParticipantsViewHolder.Callback
}