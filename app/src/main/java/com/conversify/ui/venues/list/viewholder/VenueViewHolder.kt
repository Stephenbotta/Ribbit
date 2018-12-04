package com.conversify.ui.venues.list.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.gone
import com.conversify.extensions.visible
import com.conversify.utils.AppUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue.view.*

class VenueViewHolder(itemView: View,
                      private val glide: GlideRequests,
                      private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onVenueClicked(venue) }

        itemView.btnMore.setOnClickListener { }
    }

    private lateinit var venue: VenueDto

    fun bind(venue: VenueDto) {
        this.venue = venue

        if (venue.isMember == true) {
            itemView.btnMore.visible()
            itemView.tvDistance.gone()
        } else {
            itemView.btnMore.gone()

            // For nearby listVenues, if distance does not exist then hide the distance view.
            if (venue.distance == null) {
                itemView.tvDistance.gone()
            } else {
                itemView.tvDistance.visible()
            }
        }

        if (venue.isPrivate == true) {
            itemView.ivPrivate.visible()
        } else {
            itemView.ivPrivate.gone()
        }

        glide.load(venue.imageUrl?.original)
                .thumbnail(glide.load(venue.imageUrl?.thumbnail))
                .placeholder(R.color.greyImageBackground)
                .error(R.color.greyImageBackground)
                .into(itemView.ivVenue)

        itemView.tvVenueName.text = venue.name
        itemView.tvVenueLocation.text = AppUtils.getFormattedAddress(venue.locationName, venue.locationAddress)

        val memberCount = venue.memberCount ?: 0
        itemView.tvActiveMembers.text = itemView.context.resources.getQuantityString(R.plurals.active_members_with_count, memberCount, memberCount)
        itemView.tvDistance.text = itemView.context.getString(R.string.distance_mile_with_value, venue.distance)
    }

    interface Callback {
        fun onVenueClicked(venue: VenueDto)
    }
}