package com.pulse.ui.groups.details.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import com.pulse.R
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.extensions.isNetworkActiveWithMessage
import kotlinx.android.synthetic.main.item_group_details_header.view.*

class GroupDetailsHeaderViewHolder(itemView: View,
                                   private val callback: Callback) : RecyclerView.ViewHolder(itemView), CompoundButton.OnCheckedChangeListener {
    init {
        itemView.switchNotifications.setOnCheckedChangeListener(this)
    }

    private lateinit var venue: GroupDto

    fun bind(venue: GroupDto) {
        this.venue = venue

        itemView.apply {
            updateNotificationsState(venue.notification ?: false)
            tvDescription.text = venue.description
            tvLabelMembers.text = context.getString(R.string.venue_details_label_members_with_count, venue.memberCount)
        }
    }

    private fun updateNotificationsState(isEnabled: Boolean) {
        itemView.switchNotifications.setOnCheckedChangeListener(null)
        itemView.switchNotifications.isChecked = isEnabled
        itemView.switchNotifications.setOnCheckedChangeListener(this@GroupDetailsHeaderViewHolder)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (itemView.context.isNetworkActiveWithMessage()) {
            callback.onNotificationsChanged(isChecked)
        } else {
            updateNotificationsState(!isChecked)
        }
    }

    interface Callback {
        fun onNotificationsChanged(isEnabled: Boolean)
    }
}