package com.checkIt.ui.search.tag

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import kotlinx.android.synthetic.main.item_tag_search.view.*

class SearchTagViewHolder(itemView: View, private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.tvFollowedStatus.setOnClickListener { callback.onClick(profile) }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile

        itemView.tvTag.text = profile.tagName
        if (profile.isFollowing == true) {
            itemView.tvFollowedStatus.text = itemView.context.getString(R.string.people_detail_button_un_follow)
        } else {
            itemView.tvFollowedStatus.text = itemView.context.getString(R.string.people_detail_button_follow)
        }

    }

    interface Callback {
        fun onClick(profile: ProfileDto)
    }
}