package com.ribbit.ui.main.survey

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.extensions.inflate
import kotlinx.android.synthetic.main.item_profile_interest.view.*
import kotlinx.android.synthetic.main.item_survey_list.view.*

class SurveyAdapter(private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_INTEREST = 0
        private const val VIEW_TYPE_EDIT_INTERESTS = 1
    }

    private val interests = mutableListOf<InterestDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INTEREST) {
            ViewHolder(parent.inflate(R.layout.item_profile_interest))
        } else {
            SurveyListViewHolder(parent.inflate(R.layout.item_survey_list), callback)
        }
    }

    // Add one for edit interests which will be at last position
    override fun getItemCount(): Int = interests.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(interests[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position != itemCount - 1) {
            VIEW_TYPE_EDIT_INTERESTS
        } else {
            VIEW_TYPE_EDIT_INTERESTS
        }
    }

    fun displayInterests(interests: List<InterestDto>) {
        this.interests.clear()
        this.interests.addAll(interests)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private lateinit var interest: InterestDto

        fun bind(interest: InterestDto) {
            this.interest = interest
            itemView.tvInterest.text = interest.name
        }
    }

    class SurveyListViewHolder(itemView: View,
                               private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val editColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }

        init {
            itemView.setOnClickListener {

                it.findNavController().navigate(R.id.surveyDetailFragment)
            }

            itemView.tvQuestions.text = itemView.context.getString(R.string.profile_btn_edit)
            itemView.tvDuration.text = "8 hours"
     //       itemView.tvInterest.setTextColor(editColor)
     //       itemView.tvInterest.setBackgroundResource(R.drawable.background_profile_edit_interest)
        }
    }

    fun getCategoryIds(): ArrayList<String> {
        return interests.map { it.id ?: "" } as ArrayList<String>
    }

    interface Callback {
        fun onEditInterestsClicked()
    }
}