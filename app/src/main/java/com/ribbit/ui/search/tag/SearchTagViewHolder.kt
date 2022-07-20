package com.ribbit.ui.search.tag

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.ui.loginsignup.ProfileDto
import kotlinx.android.synthetic.main.item_tag_search.view.*

class SearchTagViewHolder(itemView: View, private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.tvFollowedStatus.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                profile.isFollowing = !(profile.isFollowing ?: false)
                setFollowState(profile.isFollowing ?: false)
                callback.onClick(profile)
            }
        }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile
        itemView.tvTag.text = profile.tagName
        setFollowState(profile.isFollowing ?: false)
    }

    private fun setFollowState(isFollowing: Boolean) {
        itemView.tvFollowedStatus.text = if (isFollowing) {
            itemView.context.getString(R.string.people_detail_button_un_follow)
        } else {
            itemView.context.getString(R.string.people_detail_button_follow)
        }
    }

    interface Callback {
        fun onClick(profile: ProfileDto)
    }
}