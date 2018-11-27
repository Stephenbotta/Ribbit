package com.conversify.ui.venues.details

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.inflate
import com.conversify.ui.venues.details.viewholder.VenueDetailsExitGroupViewHolder
import com.conversify.ui.venues.details.viewholder.VenueDetailsHeaderViewHolder
import com.conversify.ui.venues.details.viewholder.VenueDetailsMemberViewHolder
import com.conversify.utils.GlideRequests

class VenueDetailsAdapter(val glide: GlideRequests,
                          private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MEMBER = 1
        private const val VIEW_TYPE_EXIT_GROUP = 2
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> VenueDetailsHeaderViewHolder(parent.inflate(R.layout.item_venue_details_header), callback)

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
                if (item is VenueMemberDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is VenueDto -> VIEW_TYPE_HEADER
            is VenueMemberDto -> VIEW_TYPE_MEMBER
            else -> VIEW_TYPE_EXIT_GROUP
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    interface Callback : VenueDetailsHeaderViewHolder.Callback,
            VenueDetailsMemberViewHolder.Callback,
            VenueDetailsExitGroupViewHolder.Callback
}