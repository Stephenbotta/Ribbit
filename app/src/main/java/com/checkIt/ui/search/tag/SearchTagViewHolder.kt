package com.checkIt.ui.search.tag

import android.support.v7.widget.RecyclerView
import android.view.View
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_tag_search.view.*

class SearchTagViewHolder(itemView: View,
                          private val glide: GlideRequests,
                          private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.tvFollowedStatus.setOnClickListener { callback.onClick(adapterPosition,profile) }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile

        itemView.tvTag.text = profile.tagName
        if (profile.isFollowing==true) {
            itemView.tvFollowedStatus.text = itemView.context.getString(R.string.people_detail_button_un_follow)
        } else {
            itemView.tvFollowedStatus.text = itemView.context.getString(R.string.people_detail_button_follow)
        }

    }

    interface Callback {
        fun onClick(position: Int,profile: ProfileDto)
    }
}