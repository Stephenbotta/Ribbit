package com.conversify.ui.venues.list.viewholder

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.loginsignup.ProfileDto
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
                itemView.tvRequestStatus.setText(R.string.venues_label_rejected)
                itemView.tvRequestStatus.setTextColor(requestRejectedColor)
            }

            else -> {
                itemView.tvRequestStatus.gone()
            }
        }

        glide.load(venue.imageUrl?.thumbnail)
                .into(itemView.ivVenue)

        itemView.tvVenueName.text = venue.name
        itemView.tvVenueLocation.text = AppUtils.getFormattedAddress(venue.locationName, venue.locationAddress)

        val memberCount = venue.memberCount ?: 0
        itemView.tvActiveMembers.text = itemView.context.resources.getQuantityString(R.plurals.members_with_count, memberCount, memberCount)
    }

    interface Callback {
        fun onVenueClicked(venue: VenueDto)
    }
}