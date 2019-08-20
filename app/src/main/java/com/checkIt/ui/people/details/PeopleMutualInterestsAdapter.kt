package com.checkIt.ui.people.details

import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.extensions.inflate
import kotlinx.android.synthetic.main.item_profile_interest.view.*

class PeopleMutualInterestsAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val mutualInterests = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {

        return ViewHolder(parent.inflate(R.layout.item_profile_interest))

    }

    // Add one for edit interests which will be at last position
    override fun getItemCount(): Int = mutualInterests.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(mutualInterests[position])
        }
    }

    fun displayMutualInterests(mutualInterests: List<InterestDto>) {
        this.mutualInterests.clear()
        this.mutualInterests.addAll(mutualInterests)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private lateinit var mutualInterest: InterestDto

        fun bind(mutualInterest: InterestDto) {
            this.mutualInterest = mutualInterest
            itemView.tvInterest.text = mutualInterest.name
        }
    }

}