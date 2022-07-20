package com.ribbit.ui.groups.listing

import android.view.ViewGroup
import com.ribbit.R
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.SuggestedGroupsDto
import com.ribbit.data.remote.models.groups.YourGroupsLabelDto
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.extensions.inflate
import com.ribbit.ui.groups.listing.viewholders.SuggestedGroupsParentViewHolder
import com.ribbit.ui.groups.listing.viewholders.YourGroupViewHolder
import com.ribbit.ui.groups.listing.viewholders.YourGroupsLabelViewHolder
import com.ribbit.utils.GlideRequests

class GroupsAdapter(private val glide: GlideRequests,
                    private val callback: Callback,
                    private var ownProfile: ProfileDto
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SUGGESTED_GROUPS = 0
        private const val VIEW_TYPE_LABEL_YOUR_GROUPS = 1
        private const val VIEW_TYPE_YOUR_GROUP = 2
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SUGGESTED_GROUPS -> SuggestedGroupsParentViewHolder(parent.inflate(R.layout.item_suggested_groups_parent), glide, callback)

            VIEW_TYPE_LABEL_YOUR_GROUPS -> YourGroupsLabelViewHolder(parent.inflate(R.layout.item_groups_label_your_groups))

            VIEW_TYPE_YOUR_GROUP -> YourGroupViewHolder(parent.inflate(R.layout.item_groups_your_group), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SuggestedGroupsParentViewHolder -> {
                if (item is SuggestedGroupsDto) {
                    holder.bind(item)
                }
            }

            is YourGroupViewHolder -> {
                if (item is GroupDto) {
                    holder.bind(item, ownProfile)
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

    /**
     * This is called when user updates the profile and needs to reflect latest profile image to the recycler view items.
     * */
    fun updateOwnProfile(profile: ProfileDto) {
        ownProfile = profile
        notifyDataSetChanged()
    }

    fun updateSuggestedGroup(updatedGroup: GroupDto) {
        val firstItem = items.firstOrNull()
        if (firstItem is SuggestedGroupsDto) {
            val suggestedGroups = firstItem.groups
            val index = suggestedGroups.indexOfFirst { it.id == updatedGroup.id }
            if (index != -1) {
                suggestedGroups[index] = updatedGroup
                notifyDataSetChanged()
            }
        }
    }

    interface Callback : SuggestedGroupsParentViewHolder.Callback, YourGroupViewHolder.Callback
}