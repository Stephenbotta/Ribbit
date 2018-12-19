package com.conversify.ui.creategroup.addparticipants

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests

class AddParticipantsAdapter(private val glide: GlideRequests,
                             private val clickableParticipants: Boolean = true) : RecyclerView.Adapter<ParticipantViewHolder>() {
    private val participants = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(parent.inflate(R.layout.item_group_participant), glide, clickableParticipants)
    }

    override fun getItemCount(): Int = participants.size

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
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
}