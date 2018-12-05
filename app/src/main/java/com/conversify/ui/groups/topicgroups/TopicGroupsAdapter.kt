package com.conversify.ui.groups.topicgroups

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.extensions.gone
import com.conversify.extensions.inflate
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_suggested_groups_child.view.*

class TopicGroupsAdapter(private val glide: GlideRequests,
                         private val callback: (GroupDto) -> Unit) : RecyclerView.Adapter<TopicGroupsAdapter.ViewHolder>() {
    private val groups = mutableListOf<GroupDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_topic_group), glide, callback)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    fun displayGroups(groups: List<GroupDto>) {
        this.groups.clear()
        this.groups.addAll(groups)
        notifyDataSetChanged()
    }

    fun addGroups(groups: List<GroupDto>) {
        val oldListSize = this.groups.size
        this.groups.addAll(groups)
        notifyItemRangeInserted(oldListSize, groups.size)
    }

    class ViewHolder(itemView: View,
                     private val glide: GlideRequests,
                     private val callback: (GroupDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    callback(group)
                }
            }
        }

        private lateinit var group: GroupDto

        fun bind(group: GroupDto) {
            this.group = group

            glide.load(group.imageUrl?.original)
                    .into(itemView.ivGroup)
            itemView.tvGroupName.text = group.name

            val members = group.memberCount ?: 0
            itemView.tvMemberCount.text = itemView.context.resources
                    .getQuantityString(R.plurals.members_with_count, members, members)

            if (group.isPrivate == true) {
                itemView.ivPrivate.visible()
            } else {
                itemView.ivPrivate.gone()
            }

            itemView.ivFavourite.setImageResource(if (group.isMember == true) {
                R.drawable.ic_star_selected
            } else {
                R.drawable.ic_star_normal
            })
        }
    }

    interface Callback {
        fun onGroupClicked(group: GroupDto)
    }
}