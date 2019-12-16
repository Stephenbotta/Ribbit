package com.ribbit.ui.main.survey

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.ribbit.R

import com.ribbit.data.remote.models.survey.SurveyInfo
import com.ribbit.extensions.inflate
import com.ribbit.extensions.shortToast
import kotlinx.android.synthetic.main.item_profile_interest.view.*
import kotlinx.android.synthetic.main.item_survey_list.view.*

class SurveyAdapter(private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_INTEREST = 0
        private const val VIEW_TYPE_EDIT_INTERESTS = 1
    }

    private val interests = mutableListOf<SurveyInfo>()

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
        if (holder is SurveyListViewHolder) {
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

    fun displayInterests(interests: List<SurveyInfo>) {
        this.interests.clear()
        this.interests.addAll(interests)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private lateinit var interest: SurveyInfo

        fun bind(interest: SurveyInfo) {
            this.interest = interest
            itemView.tvInterest.text = interest.name
        }
    }

    class SurveyListViewHolder(itemView: View,
                               private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
      //  private val editColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }

        private lateinit var interest: SurveyInfo

        fun bind(interest: SurveyInfo) {
            this.interest = interest
            itemView.tvQuestions.text = interest.name
            itemView.tvQuizNo.text = "${interest.totalTime} mins"
        }

        init {
            itemView.setOnClickListener {

                if (interest.questionCount == 0){
                    itemView.context.shortToast("This survey has empty data now...")
                    return@setOnClickListener
                }

                val bundle = Bundle()
                bundle.putString(SurveyDetailFragment.SURVEY_ID,interest._id)
                bundle.putInt(SurveyDetailFragment.SURVEY_TIME,interest.totalTime ?: 10)

                it.findNavController().navigate(R.id.surveyDetailFragment,bundle)
            }


     //       itemView.tvInterest.setTextColor(editColor)
     //       itemView.tvInterest.setBackgroundResource(R.drawable.background_profile_edit_interest)
        }
    }

//    fun getCategoryIds(): ArrayList<String> {
//        return interests.map { it.id ?: "" } as ArrayList<String>
//    }

    interface Callback {
        fun onEditInterestsClicked()
    }
}