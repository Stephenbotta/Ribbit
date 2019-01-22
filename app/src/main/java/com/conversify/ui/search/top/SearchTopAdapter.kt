package com.conversify.ui.search.top

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests

class SearchTopAdapter(private val glide: GlideRequests,
                       private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> SearchTopLabelViewHolder(parent.inflate(R.layout.item_venue_your_venues_label))

            VIEW_TYPE_MY_VENUE -> SearchTopViewHolder(parent.inflate(R.layout.item_top_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchTopViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item)
                }
            }

//            is VenuesNearLabelViewHolder -> {
//                if (item is VenuesNearYouDto) {
//                    holder.bind(item)
//                }
//            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {

            is ProfileDto -> VIEW_TYPE_MY_VENUE
            else -> VIEW_TYPE_LABEL_YOUR_VENUES
        }
    }

    fun getVenuesCount(): Int = items.count { it is ProfileDto }

    private fun getYourVenuesCount(): Int = items.count { it is VenueDto && it.isMember == true }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(item: List<Any>) {
        val oldListSize = this.items.size
        this.items.addAll(item)
        notifyItemRangeInserted(oldListSize, item.size)
    }

    fun removeVenue(venue: VenueDto) {
        val index = items.indexOfFirst { it is VenueDto && it.id == venue.id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }

        // If your venues count is 0, then remove the "YOUR VENUES" label item.
        if (getYourVenuesCount() == 0) {
            val yourVenuesLabelIndex = items.indexOfFirst { it is YourVenuesDto }
            if (yourVenuesLabelIndex != -1) {
                items.removeAt(yourVenuesLabelIndex)
                notifyItemRemoved(yourVenuesLabelIndex)
            }
        }
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

    interface Callback : SearchTopViewHolder.Callback
}