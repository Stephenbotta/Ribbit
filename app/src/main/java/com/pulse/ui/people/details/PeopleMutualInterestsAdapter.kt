package com.pulse.ui.people.details

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.loginsignup.InterestDto
import com.pulse.extensions.inflate
import kotlinx.android.synthetic.main.item_profile_interest.view.*

class PeopleMutualInterestsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mutualInterests = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ViewHolder(parent.inflate(R.layout.item_profile_interest))

    }

    // Add one for edit interests which will be at last position
    override fun getItemCount(): Int = mutualInterests.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(mutualInterests[position])
        }
    }

    fun displayMutualInterests(mutualInterests: List<InterestDto>) {
        this.mutualInterests.clear()
        this.mutualInterests.addAll(mutualInterests)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var mutualInterest: InterestDto

        fun bind(mutualInterest: InterestDto) {
            this.mutualInterest = mutualInterest
            itemView.tvInterest.text = mutualInterest.name
        }
    }

}