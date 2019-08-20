package com.checkIt.ui.main.searchusers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.checkIt.R
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.extensions.inflate
import kotlinx.android.synthetic.main.item_profile_interest.view.*

class InterestsAdapter(private val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<InterestsAdapter.ViewHolder>() {
    private val interests = ArrayList<InterestDto>()
    private val userInterests by lazy {
        ArrayList<InterestDto>().also {
            it.addAll(UserManager.getProfile().interests ?: emptyList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_profile_interest), context, userInterests)
    }

    override fun getItemCount(): Int {
        return interests.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindInterests(interests[position])
    }

    class ViewHolder(itemView: View, private val context: Context, private val userInterests: ArrayList<InterestDto>) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val mutualInterestColor by lazy { ContextCompat.getColor(context, R.color.colorPrimary) }
        private val defaultInterestColor by lazy { ContextCompat.getColor(context, R.color.textGray) }

        fun bindInterests(interest: InterestDto) {
            itemView.tvInterest.text = interest.name
//            for (index in userInterests.indices) {
//                if (userInterests[index].id == interest.id) {
//                    setSelectedForMutualInterests(true)
//                }
//            }
//                if (interest.id == userInterests[position].id) {
//                    setSelectedForMutualInterests(true)
//                } else {
//                    setSelectedForMutualInterests(false)
//                }
            val position = userInterests.indexOfFirst { it.id == interest.id }
            if (position != -1) {
                setSelectedForMutualInterests(true)
            }/* else {
                setSelectedForMutualInterests(false)
            }*/
        }

        private fun setSelectedForMutualInterests(isMutualInterest: Boolean) {
            if (isMutualInterest) {
                itemView.tvInterest.setTextColor(mutualInterestColor)
                itemView.tvInterest.setBackgroundResource(R.drawable.background_profile_edit_interest)
            } else {
                itemView.tvInterest.setTextColor(defaultInterestColor)
                itemView.tvInterest.setBackgroundResource(R.drawable.background_profile_interest)
            }
        }
    }

    fun displayMutualInterests(interests: List<InterestDto>) {
        this.interests.clear()
        this.interests.addAll(interests)
        notifyDataSetChanged()
    }
}