package com.ribbit.ui.profile.settings.hideinfo.hidestatus

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class HideStatusAdapter(private val glide: GlideRequests) : RecyclerView.Adapter<HideStatusAdapter.ViewHolder>() {

    private val users = ArrayList<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_top_search), glide)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    fun displayCategories(users: List<ProfileDto>) {
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests) : RecyclerView.ViewHolder(itemView) {

        private val selectedUserColor by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }
        private val unselectedUserColor by lazy { ContextCompat.getColor(itemView.context, R.color.textGray) }

        init {
            itemView.setOnClickListener {
                profile.isSelected = profile.isSelected.not()
                setSelected(profile.isSelected)
            }
        }

        private lateinit var profile: ProfileDto
        fun bind(profile: ProfileDto) {
            this.profile = profile
            glide.load(profile.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)

            itemView.tvUserName.text = profile.userName
            setSelected(profile.isSelected)
        }

        private fun setSelected(isSelected: Boolean) {
            if (isSelected) {
                itemView.tvUserName.setTextColor(selectedUserColor)
            } else {
                itemView.tvUserName.setTextColor(unselectedUserColor)
            }
        }
    }

    fun getSelectedUserIds(): List<String> {
        return users.filter { it.isSelected }.map { it.id ?: "" }
    }
}