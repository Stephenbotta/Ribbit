package com.pulse.ui.venues.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.R
import com.pulse.data.local.UserManager
import kotlinx.android.synthetic.main.item_venue_details_exit_group.view.*

class VenueDetailsExitGroupViewHolder(itemView: View,
                                      private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.btnExitVenue.setOnClickListener { callback.onExitVenueClicked() }
        itemView.btnArchiveVenue.setOnClickListener { callback.onArchiveVenueClicked() }
    }

    private lateinit var venue: String

    fun bind(id: String) {
        this.venue = id

        itemView.btnExitVenue.text =  if (venue == UserManager.getUserId()) {
            itemView.context.getString(R.string.venue_details_btn_delete_venue)
        } else {
            itemView.context.getString(R.string.venue_details_btn_exit_venue)
        }
    }

    interface Callback {
        fun onExitVenueClicked()
        fun onArchiveVenueClicked()
    }
}