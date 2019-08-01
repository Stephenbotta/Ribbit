package com.pulse.ui.venues.list.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.R
import com.pulse.data.remote.models.venues.VenuesNearYouDto
import kotlinx.android.synthetic.main.item_venue_venues_near_you_label.view.*

class VenuesNearLabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(nearbyVenues: VenuesNearYouDto) {
        itemView.tvLabelSuggested.text = itemView.context.getString(R.string.venues_label_suggested)
    }
}