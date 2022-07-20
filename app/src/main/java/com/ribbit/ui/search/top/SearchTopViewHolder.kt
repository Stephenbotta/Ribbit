package com.ribbit.ui.search.top

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.visible
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class SearchTopViewHolder(itemView: View,
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