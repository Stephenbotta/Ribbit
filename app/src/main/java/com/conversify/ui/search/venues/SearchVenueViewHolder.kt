package com.conversify.ui.search.venues

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.gone
import com.conversify.extensions.invisible
import com.conversify.extensions.visible
import com.conversify.utils.AppUtils
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue_search.view.*

class SearchVenueViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(adapterPosition,venue) }
    }

    private lateinit var venue: VenueDto

    fun bind(venue: VenueDto) {
        this.venue = venue

        itemView.btnMore.invisible()
        itemView.tvRequestStatus.invisible()
        itemView.ivParticipationRole.gone()

        if (venue.distance == null) {
            itemView.tvDistance.gone()
        } else {
            itemView.tvDistance.visible()
            itemView.tvDistance.text = itemView.context.getString(R.string.distance_mile_with_value, venue.distance)
        }

        if (venue.isPrivate == true) {
            itemView.ivPrivate.visible()
        } else {
            itemView.ivPrivate.gone()
        }

        glide.load(venue.imageUrl?.thumbnail)
                .into(itemView.ivVenue)

        itemView.tvVenueName.text = venue.name
        itemView.tvVenueLocation.text = AppUtils.getFormattedAddress(venue.locationName, venue.locationAddress)

        val memberCount = venue.memberCount ?: 0
        itemView.tvActiveMembers.text = itemView.context.resources.getQuantityString(R.plurals.members_with_count, memberCount, memberCount)
    }

    interface Callback {
        fun onClick(position:Int,venue: VenueDto)
    }
}