package com.checkIt.ui.groups.listing

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.extensions.inflate
import com.checkIt.ui.groups.listing.viewholders.SuggestedGroupsChildViewHolder
import com.checkIt.utils.GlideRequests

class SuggestedGroupsAdapter(private val glide: GlideRequests,
                             private val callback: Callback) : RecyclerView.Adapter<SuggestedGroupsChildViewHolder>() {
    private val groups = mutableListOf<GroupDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedGroupsChildViewHolder {
        return SuggestedGroupsChildViewHolder(parent.inflate(R.layout.item_suggested_groups_child), glide, callback)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: SuggestedGroupsChildViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    fun displayGroups(groups: List<GroupDto>) {
        this.groups.clear()
        this.groups.addAll(groups)
        notifyDataSetChanged()
    }

    interface Callback : SuggestedGroupsChildViewHolder.Callback
}