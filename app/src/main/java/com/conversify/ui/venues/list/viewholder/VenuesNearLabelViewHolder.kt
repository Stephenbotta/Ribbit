package com.conversify.ui.venues.list.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.venues.VenuesNearYouDto
import kotlinx.android.synthetic.main.item_label_venues_near_you.view.*

class VenuesNearLabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(nearbyVenues: VenuesNearYouDto) {
        itemView.tvLabelFilter.text = nearbyVenues.label ?: itemView.context.getString(R.string.venues_label_suggested)
    }
}