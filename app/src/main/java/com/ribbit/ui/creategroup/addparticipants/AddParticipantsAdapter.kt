package com.ribbit.ui.creategroup.addparticipants

import android.view.ViewGroup
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.GlideRequests

class AddParticipantsAdapter(private val glide: GlideRequests,
                             private val clickableParticipants: Boolean = true) : androidx.recyclerview.widget.RecyclerView.Adapter<ParticipantViewHolder>() {
    private val participants = mutableListOf<ProfileDto>()
    private var callback: ParticipantViewHolder.Callback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(parent.inflate(R.layout.item_group_participant), glide, clickableParticipants, callback)
    }

    override fun getItemCount(): Int = participants.size

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    /*
    * Set the callback before onCreateViewHolder is called
    * */
    fun setCallback(callback: ParticipantViewHolder.Callback) {
        this.callback = callback
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