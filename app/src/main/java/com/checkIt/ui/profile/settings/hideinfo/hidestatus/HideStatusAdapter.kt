package com.checkIt.ui.profile.settings.hideinfo.hidestatus

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.venues.VenueCategoriesHeader
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

class HideStatusAdapter(private val glide: GlideRequests, private val callback: Callback, private val ownProfile: ProfileDto, private val flag: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_top_search), glide, callback, ownProfile)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item, flag)
                }
            }
        }
    }

    fun displayCategories(item: List<Any>) {
        items.clear()
        items.addAll(item)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests,
                     private val callback: Callback, private val ownProfile: ProfileDto) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private val selectedUser by lazy { ContextCompat.getColor(itemView.context, R.color.colorPrimary) }
        private val unselectedUser by lazy { ContextCompat.getColor(itemView.context, R.color.textGray) }

        init {
            itemView.setOnClickListener { callback.onClick(adapterPosition) }
        }

        fun bind(items: ProfileDto, flag: Int) {

            glide.load(items.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)

            itemView.tvUserName.text = items.userName
            if (items.isSelected) {
                itemView.tvUserName.setTextColor(selectedUser)
            } else {
                itemView.tvUserName.setTextColor(unselectedUser)
            }

//            when (flag) {
//                ApiConstants.FLAG_PROFILE_PICTURE -> {
//                    if (items.isSelected) {
//                        itemView.tvUserName.setTextColor(selectedUser)
//                    } else {
//                        itemView.tvUserName.setTextColor(unselectedUser)
//                    }
//                }
//                ApiConstants.FLAG_PRIVATE_INFO -> {
//                    if (items.isSelected) {
//                        itemView.tvUserName.setTextColor(selectedUser)
//                    } else {
//                        itemView.tvUserName.setTextColor(unselectedUser)
//                    }
//                }
//                ApiConstants.FLAG_USERNAME -> {
//                    if (items.isSelected) {
//                        itemView.tvUserName.setTextColor(selectedUser)
//                    } else {
//                        itemView.tvUserName.setTextColor(unselectedUser)
//                    }
//                }
//                ApiConstants.FLAG_MESSAGE -> {
//
//                }
//            }
        }
    }

    interface Callback {
        fun onClick(position: Int)
    }
}