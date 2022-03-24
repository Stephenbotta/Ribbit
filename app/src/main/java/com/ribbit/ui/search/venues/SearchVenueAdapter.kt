package com.ribbit.ui.search.venues

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.GlideRequests

class SearchVenueAdapter(private val glide: GlideRequests,
                         private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> SearchVenueLabelViewHolder(parent.inflate(R.layout.item_venue_your_venues_label))

            VIEW_TYPE_MY_VENUE -> SearchVenueViewHolder(parent.inflate(R.layout.item_venue_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchVenueViewHolder -> {
                if (item is VenueDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is VenueDto -> VIEW_TYPE_MY_VENUE
            else -> VIEW_TYPE_LABEL_YOUR_VENUES
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(item: List<Any>) {
        val oldListSize = items.size
        items.addAll(item)
        notifyItemRangeInserted(oldListSize, items.size)
    }

    fun updateVenueJoinedStatus(updatedVenue: VenueDto) {
        val index = items.indexOfFirst { it is VenueDto && it.id == updatedVenue.id }
        if (index != -1) {
            val existingVenue = items[index] as VenueDto

            existingVenue.requestStatus = updatedVenue.requestStatus
            existingVenue.isMember = updatedVenue.isMember
            existingVenue.participationRole = updatedVenue.participationRole

            items[index] = updatedVenue
            notifyItemChanged(index)
        }
    }

    interface Callback : SearchVenueViewHolder.Callback
}