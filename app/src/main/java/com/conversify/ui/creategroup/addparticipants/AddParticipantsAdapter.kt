package com.conversify.ui.creategroup.addparticipants

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.gone
import com.conversify.extensions.inflate
import com.conversify.extensions.visible
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_create_group_participant.view.*

class AddParticipantsAdapter(private val glide: GlideRequests,
                             private val clickableParticipants: Boolean = true) : RecyclerView.Adapter<AddParticipantsAdapter.ViewHolder>() {
    private val participants = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_create_group_participant), glide, clickableParticipants)
    }

    override fun getItemCount(): Int = participants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    fun displayFollowers(followers: List<ProfileDto>) {
        this.participants.clear()
        this.participants.addAll(followers)
        notifyDataSetChanged()
    }

    fun getSelectedFollowers(): ArrayList<ProfileDto> {
        return ArrayList(participants.filter { it.isSelected })
    }

    class ViewHolder(itemView: View,
                     private val glide: GlideRequests,
                     clickableParticipants: Boolean) : RecyclerView.ViewHolder(itemView) {
        init {
            if (clickableParticipants) {
                itemView.setOnClickListener {
                    profile.isSelected = !profile.isSelected
                    changeSelectedState(profile.isSelected)
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
    }
}