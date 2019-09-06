package com.checkIt.ui.profile.followerandfollowing

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.extensions.gone
import com.checkIt.extensions.visible
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class FollowerAndFollowingViewHolder(itemView: View, private val glide: GlideRequests,
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

        if (profile.isAccountPrivate == true) {
            itemView.ivPrivate.visible()
        } else {
            itemView.ivPrivate.gone()
        }

    }

    interface Callback {
        fun onClick(profile: ProfileDto)
    }
}