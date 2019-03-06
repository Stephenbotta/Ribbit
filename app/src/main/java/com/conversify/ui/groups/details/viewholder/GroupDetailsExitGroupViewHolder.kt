package com.conversify.ui.groups.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.local.UserManager
import com.conversify.data.remote.models.groups.GroupDto
import kotlinx.android.synthetic.main.item_venue_details_exit_group.view.*

class GroupDetailsExitGroupViewHolder(itemView: View,
                                      private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.line.visibility = View.GONE
        itemView.btnArchiveVenue.text = itemView.context.getString(R.string.group_details_label_archive_group)
        itemView.btnExitVenue.setOnClickListener { callback.onExitVenueClicked() }
        itemView.btnArchiveVenue.setOnClickListener { callback.onArchiveVenueClicked() }
    }

    private lateinit var venue: GroupDto

    fun bind(venue: GroupDto) {
        this.venue = venue

        itemView.apply {
            btnExitVenue.text = if (venue.adminId == UserManager.getUserId()) {
                context.getString(R.string.group_details_label_delete_group)
            } else {
                context.getString(R.string.group_details_label_exit_group)
            }
        }
    }

    interface Callback {
        fun onExitVenueClicked()
        fun onArchiveVenueClicked()
    }
}