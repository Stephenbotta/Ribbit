package com.checkIt.ui.venues.list.viewholder

import android.view.View
import com.checkIt.R
import com.checkIt.data.remote.models.venues.VenuesNearYouDto
import kotlinx.android.synthetic.main.item_venue_venues_near_you_label.view.*

class VenuesNearLabelViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    fun bind(nearbyVenues: VenuesNearYouDto) {
        itemView.tvLabelSuggested.text = itemView.context.getString(R.string.venues_label_suggested)
    }
}