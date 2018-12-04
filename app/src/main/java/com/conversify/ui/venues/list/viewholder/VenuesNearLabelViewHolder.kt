package com.conversify.ui.venues.list.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.venues.VenuesNearYouDto
import kotlinx.android.synthetic.main.item_venue_venues_near_you_label.view.*

class VenuesNearLabelViewHolder(itemView: View,
                                private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.tvLabelFilter.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callback.onAppliedFilterLabelClicked()
            }
        }
    }

    fun bind(nearbyVenues: VenuesNearYouDto) {
        itemView.tvLabelFilter.text = nearbyVenues.label ?: itemView.context.getString(R.string.venues_label_suggested)
    }

    interface Callback {
        fun onAppliedFilterLabelClicked()
    }
}