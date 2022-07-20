package com.ribbit.ui.search.venues

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.AppUtils
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue_search.view.*

class SearchVenueViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(venue) }
    }

    private lateinit var venue: VenueDto

    fun bind(venue: VenueDto) {
        this.venue = venue

        if (venue.distance == null) {
            itemView.tvDistance.gone()
        } else {
            itemView.tvDistance.visible()
            itemView.tvDistance.text = itemView.context.getString(R.string.distance_mile_with_value, venue.distance)
        }

        if (venue.isMember == true) {
            itemView.ivParticipationRole.visible()
            // If status is admin, then show user's own image otherwise show a tick which denotes user is a member.
            if (venue.participationRole == ApiConstants.PARTICIPATION_ROLE_ADMIN) {
                glide.load(UserManager.getProfile().image?.thumbnail)
                        .into(itemView.ivParticipationRole)
            } else {
                itemView.ivParticipationRole.setImageResource(R.drawable.ic_tick_circle_blue)
            }
        } else {
            itemView.ivParticipationRole.gone()
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

        // Only visible when request is pending or rejected
        when (venue.requestStatus) {
            ApiConstants.REQUEST_STATUS_PENDING -> {
                itemView.tvRequestStatus.visible()
                itemView.tvRequestStatus.setText(R.string.venues_label_pending)
            }

            ApiConstants.REQUEST_STATUS_REJECTED -> {
                itemView.tvRequestStatus.gone()
                itemView.tvRequestStatus.setText(R.string.venues_label_rejected)
            }

            else -> itemView.tvRequestStatus.gone()
        }

    }

    interface Callback {
        fun onClick(venue: VenueDto)
    }
}