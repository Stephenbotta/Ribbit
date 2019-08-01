package com.pulse.ui.creategroup.addparticipants

import android.support.v7.widget.RecyclerView
import android.view.View
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.gone
import com.pulse.extensions.visible
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_group_participant.view.*

class ParticipantViewHolder(itemView: View,
                            private val glide: GlideRequests,
                            clickableParticipants: Boolean,
                            callback: Callback? = null) : RecyclerView.ViewHolder(itemView) {
    init {
        if (clickableParticipants) {
            itemView.setOnClickListener {
                profile.isSelected = !profile.isSelected
                changeSelectedState(profile.isSelected)
                callback?.onParticipantClicked(profile)
            }
        }
    }

    private lateinit var profile: ProfileDto

    fun bind(profile: ProfileDto) {
        this.profile = profile

        glide.load(profile.image?.original)
                .into(itemView.ivProfile)

        itemView.tvUserName.text = profile.userName
        changeSelectedState(profile.isSelected)
    }

    private fun changeSelectedState(selected: Boolean) {
        if (selected) {
            itemView.ivSelected.visible()
        } else {
            itemView.ivSelected.gone()
        }
    }

    interface Callback {
        fun onParticipantClicked(profile: ProfileDto)
    }
}