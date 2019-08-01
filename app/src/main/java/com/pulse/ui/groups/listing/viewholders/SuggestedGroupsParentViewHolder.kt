package com.pulse.ui.groups.listing.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.data.remote.models.groups.SuggestedGroupsDto
import com.pulse.ui.groups.listing.SuggestedGroupsAdapter
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_suggested_groups_parent.view.*

class SuggestedGroupsParentViewHolder(itemView: View,
                                      glide: GlideRequests,
                                      callback: Callback) : RecyclerView.ViewHolder(itemView) {
    private val suggestedGroupsAdapter = SuggestedGroupsAdapter(glide, callback)

    init {
        itemView.rvGroups.adapter = suggestedGroupsAdapter
    }

    fun bind(suggestedGroups: SuggestedGroupsDto) {
        suggestedGroupsAdapter.displayGroups(suggestedGroups.groups)
    }

    interface Callback : SuggestedGroupsAdapter.Callback
}