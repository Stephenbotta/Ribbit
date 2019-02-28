package com.conversify.ui.profile.followerandfollowing

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class FollowerAndFollowingViewHolder(itemView: View,
                                     private val glide: GlideRequests,
                                     private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(adapterPosition, profile) }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile
        glide.load(profile.image?.thumbnail)
                .into(itemView.ivProfilePic)
        itemView.tvUserName.text = profile.userName
    }

    interface Callback {
        fun onClick(position: Int, profile: ProfileDto)
    }
}