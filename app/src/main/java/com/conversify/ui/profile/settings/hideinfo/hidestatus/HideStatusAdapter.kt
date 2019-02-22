package com.conversify.ui.profile.settings.hideinfo.hidestatus

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueCategoriesHeader
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_top_search.view.*

/**
 * Created by Manish Bhargav
 */
class HideStatusAdapter(private val glide: GlideRequests, private val callback: Callback, private val ownProfile: ProfileDto, private val flag: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_top_search), glide, callback, ownProfile)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item, flag)
                }
            }
        }
    }

    fun displayCategories(peoples: List<Any>) {
        items.clear()
        items.addAll(peoples)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests,
                     private val callback: Callback, private val ownProfile: ProfileDto) : RecyclerView.ViewHolder(itemView) {

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
            when (flag) {
                ApiConstants.FLAG_PROFILE_PICTURE -> {
                    val size = ownProfile.imageVisibility?.size
                    if (size != 0)
                        for (i in ownProfile.imageVisibility!!.indices) {
                            if (ownProfile.imageVisibility[i].equals(items.id)) {
                                itemView.tvUserName.setTextColor(selectedUser)
                            }
                        }
                }
                ApiConstants.FLAG_PRIVATE_INFO -> {
                    ownProfile.personalInfoVisibility?.size
                }
                ApiConstants.FLAG_USERNAME -> {
                    ownProfile.nameVisibility?.size
                }
                ApiConstants.FLAG_MESSAGE -> {
                    ownProfile.tagPermission?.size
                }
            }

        }
    }

    interface Callback {
        fun onClick(position: Int)
    }
}