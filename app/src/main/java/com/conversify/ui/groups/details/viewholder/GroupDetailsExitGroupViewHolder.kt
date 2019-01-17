package com.conversify.ui.groups.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import kotlinx.android.synthetic.main.item_venue_details_exit_group.view.*

class GroupDetailsExitGroupViewHolder(itemView: View,
                                      private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.line.visibility = View.GONE
        itemView.btnArchiveVenue.text = itemView.context.getString(R.string.group_details_label_archive_group)
        itemView.btnExitVenue.text = itemView.context.getString(R.string.group_details_label_exit_group)
        itemView.btnExitVenue.setOnClickListener { callback.onExitVenueClicked() }
        itemView.btnArchiveVenue.setOnClickListener { callback.onArchiveVenueClicked() }
    }

    interface Callback {
        fun onExitVenueClicked()
        fun onArchiveVenueClicked()
    }
}