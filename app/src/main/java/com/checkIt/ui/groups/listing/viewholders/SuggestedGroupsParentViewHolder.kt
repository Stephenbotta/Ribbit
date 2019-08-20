package com.checkIt.ui.groups.listing.viewholders

import android.view.View
import com.checkIt.data.remote.models.groups.SuggestedGroupsDto
import com.checkIt.ui.groups.listing.SuggestedGroupsAdapter
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_suggested_groups_parent.view.*

class SuggestedGroupsParentViewHolder(itemView: View,
                                      glide: GlideRequests,
                                      callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val suggestedGroupsAdapter = SuggestedGroupsAdapter(glide, callback)

    init {
        itemView.rvGroups.adapter = suggestedGroupsAdapter
    }

    fun bind(suggestedGroups: SuggestedGroupsDto) {
        suggestedGroupsAdapter.displayGroups(suggestedGroups.groups)
    }

    interface Callback : SuggestedGroupsAdapter.Callback
}