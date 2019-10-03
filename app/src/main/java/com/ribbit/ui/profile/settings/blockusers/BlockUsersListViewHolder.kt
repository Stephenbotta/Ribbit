package com.ribbit.ui.profile.settings.blockusers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class BlockUsersListViewHolder(itemView: View,
                               private val glide: GlideRequests,
                               private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener { callback.onClick(profile) }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile
        glide.load(profile.image?.thumbnail)
                .into(itemView.ivProfilePic)
        itemView.tvUserName.text = profile.userName
    }

    interface Callback {
        fun onClick(profile: ProfileDto)
    }
}