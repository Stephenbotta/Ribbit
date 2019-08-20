package com.checkIt.ui.venues.list.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import com.checkIt.R
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.venues.VenueDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.visible
import com.checkIt.utils.AppUtils
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_venue.view.*

class VenueViewHolder(itemView: View,
                      private val glide: GlideRequests,
                      private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onVenueClicked(venue) }

        itemView.btnMore.setOnClickListener { }
    }

    private val requestPendingColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }
    private val requestRejectedColor by lazy { ContextCompat.getColor(itemView.context, R.color.red) }
    private lateinit var venue: VenueDto

    fun bind(venue: VenueDto, ownProfile: ProfileDto) {
        this.venue = venue

        if (venue.isMember == true) {
            itemView.btnMore.gone()
            itemView.tvDistance.gone()
            itemView.ivParticipationRole.visible()

            // If status is admin, then show user's own image otherwise show a tick which denotes user is a member.
            if (venue.participationRole == ApiConstants.PARTICIPATION_ROLE_ADMIN) {
                glide.load(ownProfile.image?.thumbnail)
                        .into(itemView.ivParticipationRole)
            } else {
                itemView.ivParticipationRole.setImageResource(R.drawable.ic_tick_circle_blue)
            }
        } else {
            itemView.btnMore.gone()
            itemView.ivParticipationRole.gone()

            // For nearby list venues, if distance does not exist then hide the distance view.
            if (venue.distance == null) {
                itemView.tvDistance.gone()
            } else {
                itemView.tvDistance.visible()
                itemView.tvDistance.text = itemView.context.getString(R.string.distance_mile_with_value, venue.distance)
            }
        }

        if (venue.isPrivate == true) {
            itemView.ivPrivate.visible()
        } else {
            itemView.ivPrivate.gone()
        }

        // Only visible when request is pending or rejected
        when (venue.requestStatus) {
            ApiConstants.REQUEST_STATUS_PENDING -> {
                itemView.tvRequestStatus.visible()
                itemView.tvRequestStatus.setText(R.string.venues_label_pending)
                itemView.tvRequestStatus.setTextColor(requestPendingColor)
            }

            ApiConstants.REQUEST_STATUS_REJECTED -> {
                itemView.tvRequestStatus.visible()
                itemView.tvRequestStatus.text = ""
                itemView.tvRequestStatus.setTextColor(requestRejectedColor)
            }

            else -> {
                itemView.tvRequestStatus.gone()
            }
        }

        glide.load(venue.imageUrl?.thumbnail)
                .into(itemView.ivVenue)
        val name = venue.name?.substring(0, 1)?.toUpperCase() + venue.name?.substring(1)
        itemView.tvVenueName.text = name
        itemView.tvVenueLocation.text = AppUtils.getFormattedAddress(venue.locationName, venue.locationAddress)

        val memberCount = venue.memberCount ?: 0
        itemView.tvActiveMembers.text = itemView.context.resources.getQuantityString(R.plurals.members_with_count_venue, memberCount, memberCount)
    }

    interface Callback {
        fun onVenueClicked(venue: VenueDto)
    }
}