package com.conversify.ui.venues.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import com.conversify.R
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.toLatLng
import com.conversify.utils.MapUtils
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
            updateNotificationState(venue.notification ?: false)
            tvLocationName.text = venue.locationName
            tvLocationAddress.text = venue.locationAddress
            tvLabelMembers.text = context.getString(R.string.venue_details_label_members_with_count, venue.memberCount)
        }
    }

    fun updateNotificationState(isEnabled: Boolean) {
        itemView.switchNotifications.setOnCheckedChangeListener(null)
        itemView.switchNotifications.isChecked = isEnabled
        itemView.switchNotifications.setOnCheckedChangeListener(this@VenueDetailsHeaderViewHolder)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        callback.onNotificationChanged(isChecked)
    }

    interface Callback {
        fun onNotificationChanged(isEnabled: Boolean)
    }
}