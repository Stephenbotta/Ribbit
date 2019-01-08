package com.conversify.ui.main.profile

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.inflate
import kotlinx.android.synthetic.main.item_profile_interest.view.*

class ProfileInterestsAdapter(private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_INTEREST = 0
        private const val VIEW_TYPE_EDIT_INTERESTS = 1
    }

    private val interests = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INTEREST) {
            ViewHolder(parent.inflate(R.layout.item_profile_interest))
        } else {
            EditInterestsViewHolder(parent.inflate(R.layout.item_profile_interest), callback)
        }
    }

    // Add one for edit interests which will be at last position
    override fun getItemCount(): Int = interests.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(interests[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position != itemCount - 1) {
            VIEW_TYPE_INTEREST
        } else {
            VIEW_TYPE_EDIT_INTERESTS
        }
    }

    fun displayInterests(interests: List<InterestDto>) {
        this.interests.clear()
        this.interests.addAll(interests)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var interest: InterestDto

        fun bind(interest: InterestDto) {
            this.interest = interest
            itemView.tvInterest.text = interest.name
        }
    }

    class EditInterestsViewHolder(itemView: View,
                                  private val callback: Callback) : RecyclerView.ViewHolder(itemView) {
        private val editColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }

        init {
            itemView.setOnClickListener {
                callback.onEditInterestsClicked()
            }

            itemView.tvInterest.text = itemView.context.getString(R.string.profile_btn_edit)
            itemView.tvInterest.setTextColor(editColor)
            itemView.tvInterest.setBackgroundResource(R.drawable.background_profile_edit_interest)
        }
    }

    interface Callback {
        fun onEditInterestsClicked()
    }
}