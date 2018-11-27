package com.conversify.ui.venues.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_venue_details_exit_group.view.*

class VenueDetailsExitGroupViewHolder(itemView: View,
                                      private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.btnExitGroup.setOnClickListener { callback.onExitGroupClicked() }
    }

    interface Callback {
        fun onExitGroupClicked()
    }
}