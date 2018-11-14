package com.conversify.ui.venues.list

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.inflate
import com.conversify.ui.venues.list.viewholder.VenueViewHolder
import com.conversify.ui.venues.list.viewholder.VenuesNearLabelViewHolder
import com.conversify.ui.venues.list.viewholder.YourVenuesLabelViewHolder
import com.conversify.utils.GlideRequests

class VenuesListAdapter(private val glide: GlideRequests) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
        private const val VIEW_TYPE_LABEL_VENUES_NEAR = 2
        private const val VIEW_TYPE_NEARBY_VENUE = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> YourVenuesLabelViewHolder(parent.inflate(R.layout.item_label_your_venues))

            VIEW_TYPE_MY_VENUE, VIEW_TYPE_NEARBY_VENUE -> VenueViewHolder(parent.inflate(R.layout.item_venue), glide)

            VIEW_TYPE_LABEL_VENUES_NEAR -> VenuesNearLabelViewHolder(parent.inflate(R.layout.item_label_venues_near_you))

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is VenueViewHolder && item is VenueDto) {
            holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is YourVenuesDto -> VIEW_TYPE_LABEL_YOUR_VENUES

            is VenueDto -> {
                if (item.myVenue) {
                    VIEW_TYPE_MY_VENUE
                } else {
                    VIEW_TYPE_NEARBY_VENUE
                }
            }

            else -> VIEW_TYPE_LABEL_VENUES_NEAR
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}