package com.conversify.ui.groups.listing

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.SuggestedGroupsDto
import com.conversify.data.remote.models.groups.YourGroupsLabelDto
import com.conversify.extensions.inflate
import com.conversify.ui.groups.listing.viewholders.SuggestedGroupsParentViewHolder
import com.conversify.ui.groups.listing.viewholders.YourGroupViewHolder
import com.conversify.ui.groups.listing.viewholders.YourGroupsLabelViewHolder
import com.conversify.utils.GlideRequests

class GroupsAdapter(private val glide: GlideRequests,
                    private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SUGGESTED_GROUPS = 0
        private const val VIEW_TYPE_LABEL_YOUR_GROUPS = 1
        private const val VIEW_TYPE_YOUR_GROUP = 2
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SUGGESTED_GROUPS -> SuggestedGroupsParentViewHolder(parent.inflate(R.layout.item_suggested_groups_parent), glide, callback)

            VIEW_TYPE_LABEL_YOUR_GROUPS -> YourGroupsLabelViewHolder(parent.inflate(R.layout.item_groups_label_your_groups))

            VIEW_TYPE_YOUR_GROUP -> YourGroupViewHolder(parent.inflate(R.layout.item_groups_your_group), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SuggestedGroupsParentViewHolder -> {
                if (item is SuggestedGroupsDto) {
                    holder.bind(item)
                }
            }

            is YourGroupViewHolder -> {
                if (item is GroupDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is SuggestedGroupsDto -> VIEW_TYPE_SUGGESTED_GROUPS
            is YourGroupsLabelDto -> VIEW_TYPE_LABEL_YOUR_GROUPS
            else -> VIEW_TYPE_YOUR_GROUP
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun resetUnreadCount(group: GroupDto) {
        val index = items.indexOfFirst { it is GroupDto && it.id == group.id }
        if (index != -1) {
            val existingGroup = items[index] as GroupDto
            existingGroup.unreadCount = 0
            notifyItemChanged(index)
        }
    }

    interface Callback : SuggestedGroupsParentViewHolder.Callback, YourGroupViewHolder.Callback
}