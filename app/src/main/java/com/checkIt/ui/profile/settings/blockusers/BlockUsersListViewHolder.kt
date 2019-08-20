package com.checkIt.ui.profile.settings.blockusers

import android.view.View
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class BlockUsersListViewHolder(itemView: View,
                               private val glide: GlideRequests,
                               private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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