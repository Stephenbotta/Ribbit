package com.ribbit.ui.groups.details.viewholder

import android.view.View
import com.ribbit.R
import com.ribbit.data.local.UserManager
import kotlinx.android.synthetic.main.item_venue_details_exit_group.view.*

class GroupDetailsExitGroupViewHolder(itemView: View,
                                      private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.line.visibility = View.GONE
        itemView.btnArchiveVenue.text = itemView.context.getString(R.string.group_details_label_archive_group)
        itemView.btnExitVenue.setOnClickListener { callback.onExitVenueClicked() }
        itemView.btnArchiveVenue.setOnClickListener { callback.onArchiveVenueClicked() }
    }

    private lateinit var group: String

    fun bind(id: String) {
        this.group = id

        itemView.btnExitVenue.text = if (group == UserManager.getUserId()) {
            itemView.context.getString(R.string.group_details_label_delete_group)
        } else {
            itemView.context.getString(R.string.group_details_label_exit_group)
        }
    }


    interface Callback {
        fun onExitVenueClicked()
        fun onArchiveVenueClicked()
    }
}