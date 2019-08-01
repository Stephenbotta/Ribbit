package com.pulse.ui.groups.topicgroups

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.extensions.gone
import com.pulse.extensions.inflate
import com.pulse.extensions.visible
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_topic_group.view.*

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

    fun updateGroup(group: GroupDto) {
        val index = groups.indexOfFirst { it.id == group.id }
        if (index != -1) {
            notifyItemChanged(index)
        }
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

            // Only visible when request is pending or rejected
            when (group.requestStatus) {
                ApiConstants.REQUEST_STATUS_PENDING -> {
                    itemView.tvRequestStatus.visible()
                    itemView.tvRequestStatus.setText(R.string.venues_label_pending)
                }

                ApiConstants.REQUEST_STATUS_REJECTED -> {
                    itemView.tvRequestStatus.gone()
                    itemView.tvRequestStatus.setText(R.string.venues_label_rejected)
                }

                else -> {
                    itemView.tvRequestStatus.gone()
                }
            }

            itemView.ivFavourite.setImageResource(if (group.isMember == true) {
                R.drawable.ic_star_selected
            } else {
                R.drawable.ic_star_normal
            })
        }
    }
}