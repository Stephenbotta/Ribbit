package com.ribbit.ui.main.survey

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R

import com.ribbit.data.remote.models.survey.SurveyList
import com.ribbit.extensions.inflate
import com.ribbit.extensions.shortToast
import kotlinx.android.synthetic.main.item_survey_list.view.*

class SurveyAdapter(private val context: Context) : RecyclerView.Adapter<SurveyAdapter.ViewHolder>() {
    private val surveys = mutableListOf<SurveyList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_survey_list), context)
    }

    override fun getItemCount(): Int = surveys.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(surveys[position])
    }

    fun displaySurveys(surveys: List<SurveyList>) {
        this.surveys.clear()
        this.surveys.addAll(surveys)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private lateinit var survey: SurveyList
        fun bind(survey: SurveyList) {
            this.survey = survey
            itemView.tvQuestion.text = survey.name
            itemView.tvQuizNo.text = context.getString(R.string.survey_time, survey.totalTime ?: 0)
        }

        init {
            itemView.setOnClickListener {
                if (survey.questionCount == 0) {
                    itemView.context.shortToast(context.getString(R.string.this_survey_has_empty_data_now))
                    return@setOnClickListener
                }

                val bundle = Bundle()
                bundle.putParcelable(SurveyDetailFragment.SURVEY, survey)
                it.findNavController().navigate(R.id.surveyDetailFragment, bundle)
            }
        }
    }
}