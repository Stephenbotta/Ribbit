package com.checkIt.ui.venues.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import com.checkIt.R
import com.checkIt.data.remote.models.venues.VenueDto
import com.checkIt.extensions.isNetworkActiveWithMessage
import com.checkIt.extensions.toLatLng
import com.checkIt.utils.DateTimeUtils
import com.checkIt.utils.MapUtils
import kotlinx.android.synthetic.main.item_venue_details_header.view.*

class VenueDetailsHeaderViewHolder(itemView: View,
                                   private val callback: Callback) : RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
    init {
        itemView.btnDirections.setOnClickListener {
            val location = venue.venueLocation.toLatLng()
            MapUtils.openGoogleMaps(itemView.context, location.latitude, location.longitude)
        }

        itemView.switchNotifications.setOnCheckedChangeListener(this)
    }

    private lateinit var venue: VenueDto

    fun bind(venue: VenueDto) {
        this.venue = venue

        itemView.apply {
            updateNotificationsState(venue.notification ?: false)
            tvDateAndTime.text = DateTimeUtils.formatVenueDetailsDateTime(venue.venueDateTime)
            tvLocationName.text = venue.locationName
            tvLocationAddress.text = venue.locationAddress
            tvLabelMembers.text = context.getString(R.string.venue_details_label_members_with_count, venue.memberCount)
        }
    }

    private fun updateNotificationsState(isEnabled: Boolean) {
        itemView.switchNotifications.setOnCheckedChangeListener(null)
        itemView.switchNotifications.isChecked = isEnabled
        itemView.switchNotifications.setOnCheckedChangeListener(this@VenueDetailsHeaderViewHolder)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (itemView.context.isNetworkActiveWithMessage()) {
            callback.onNotificationsChanged(isChecked)
        } else {
            updateNotificationsState(!isChecked)
        }
    }

    interface Callback {
        fun onNotificationsChanged(isEnabled: Boolean)
    }
}