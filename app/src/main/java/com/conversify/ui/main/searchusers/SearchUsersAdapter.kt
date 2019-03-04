package com.conversify.ui.main.searchusers

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_search_user.view.*

class SearchUsersAdapter(private val glide: GlideRequests) : RecyclerView.Adapter<SearchUsersAdapter.ViewHolder>() {

    private val userList = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUsersAdapter.ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_search_user), glide)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindUserData(userList[position])
    }

    fun displayList(searchedUsersList: List<ProfileDto>) {
        userList.clear()
        userList.addAll(searchedUsersList)
        notifyDataSetChanged()
    }

    fun addToList(searchedUsersList: List<ProfileDto>) {
        val oldSize = userList.size
        userList.addAll(searchedUsersList)
        notifyItemRangeInserted(oldSize, userList.size - 1)
    }

    class ViewHolder(itemView: View, private val glide: GlideRequests) : RecyclerView.ViewHolder(itemView) {
        fun bindUserData(profile: ProfileDto) {
            glide.load(profile.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivProfilePic)

            itemView.tvUserName.text = profile.userName


        }
    }
}