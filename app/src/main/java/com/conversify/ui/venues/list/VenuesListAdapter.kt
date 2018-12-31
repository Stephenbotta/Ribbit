package com.conversify.ui.venues.list

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.VenuesNearYouDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.inflate
import com.conversify.ui.venues.list.viewholder.VenueViewHolder
import com.conversify.ui.venues.list.viewholder.VenuesNearLabelViewHolder
import com.conversify.ui.venues.list.viewholder.YourVenuesLabelViewHolder
import com.conversify.utils.GlideRequests

class VenuesListAdapter(private val glide: GlideRequests,
                        private val callback: Callback,
                        private var ownProfile: ProfileDto) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
        private const val VIEW_TYPE_LABEL_VENUES_NEAR = 2
        private const val VIEW_TYPE_NEARBY_VENUE = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> YourVenuesLabelViewHolder(parent.inflate(R.layout.item_venue_your_venues_label))

            VIEW_TYPE_MY_VENUE, VIEW_TYPE_NEARBY_VENUE -> VenueViewHolder(parent.inflate(R.layout.item_venue), glide, callback)

            VIEW_TYPE_LABEL_VENUES_NEAR -> VenuesNearLabelViewHolder(parent.inflate(R.layout.item_venue_venues_near_you_label))

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is VenueViewHolder -> {
                if (item is VenueDto) {
                    holder.bind(item, ownProfile)
                }
            }

            is VenuesNearLabelViewHolder -> {
                if (item is VenuesNearYouDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is YourVenuesDto -> VIEW_TYPE_LABEL_YOUR_VENUES

            is VenueDto -> {
                if (item.isMember == true) {
                    VIEW_TYPE_MY_VENUE
                } else {
                    VIEW_TYPE_NEARBY_VENUE
                }
            }

            else -> VIEW_TYPE_LABEL_VENUES_NEAR
        }
    }

    fun getVenuesCount(): Int = items.count { it is VenueDto }

    private fun getYourVenuesCount(): Int = items.count { it is VenueDto && it.isMember == true }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
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

    fun updateVenue(venue: VenueDto) {
        val index = items.indexOfFirst { it is VenueDto && it.id == venue.id }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    /**
     * This is called when user updates the profile and needs to reflect latest profile image to the recycler view items.
     * */
    fun updateOwnProfile(profile: ProfileDto) {
        ownProfile = profile
        notifyDataSetChanged()
    }

    interface Callback : VenueViewHolder.Callback
}