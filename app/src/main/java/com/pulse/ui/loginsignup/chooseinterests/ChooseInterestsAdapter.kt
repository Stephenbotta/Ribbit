package com.pulse.ui.loginsignup.chooseinterests

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.loginsignup.InterestDto
import com.pulse.extensions.inflate
import com.pulse.utils.GlideRequests
import kotlinx.android.synthetic.main.item_interest.view.*

class ChooseInterestsAdapter(private val glide: GlideRequests,
                             private val callback: Callback) : RecyclerView.Adapter<ChooseInterestsAdapter.ViewHolder>() {
    private val interests = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_interest), glide, callback)
    }

    override fun getItemCount(): Int = interests.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(interests[position])
    }

    fun displayInterests(interests: List<InterestDto>) {
        this.interests.clear()
        this.interests.addAll(interests)
        notifyDataSetChanged()
    }

    fun getSelectedInterest(): ArrayList<InterestDto> {
        return interests.filter { it.selected } as ArrayList<InterestDto>
    }

    class ViewHolder(itemView: View,
                     private val glide: GlideRequests,
                     callback: Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                interest.selected = !interest.selected
                changeSelectedState(interest.selected)
                callback.onInterestClicked(interest)
            }
        }

        private lateinit var interest: InterestDto

        fun bind(interest: InterestDto) {
            this.interest = interest

            glide.load(interest.image?.original)
                    .thumbnail(glide.load(interest.image?.thumbnail))
                    .placeholder(R.color.greyImageBackground)
                    .error(R.color.greyImageBackground)
                    .into(itemView.ivInterest)

            itemView.tvInterest.text = interest.name
            changeSelectedState(interest.selected)
        }

        private fun changeSelectedState(selected: Boolean) {
            val selectedResId = if (selected) {
                R.drawable.ic_tick
            } else {
                R.drawable.ic_add_gray_top_right
            }
            itemView.ivSelected.setImageResource(selectedResId)
        }
    }

    interface Callback {
        fun onInterestClicked(interest: InterestDto)
    }
}