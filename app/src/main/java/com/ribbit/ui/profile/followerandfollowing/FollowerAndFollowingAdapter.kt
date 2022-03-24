package com.ribbit.ui.profile.followerandfollowing

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.GlideRequests

class FollowerAndFollowingAdapter(private val glide: GlideRequests,
                                  private val callback: Callback) : RecyclerView.Adapter<FollowerAndFollowingViewHolder>() {
    private val users = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerAndFollowingViewHolder {
        return FollowerAndFollowingViewHolder(parent.inflate(R.layout.item_top_search), glide, callback)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: FollowerAndFollowingViewHolder, position: Int) {
        holder.bind(users[position])
    }

    fun displayItems(users: List<ProfileDto>) {
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    fun addMoreItems(users: List<ProfileDto>) {
        val oldListSize = this.users.size
        this.users.addAll(users)
        notifyItemRangeInserted(oldListSize, this.users.size)
    }

    interface Callback : FollowerAndFollowingViewHolder.Callback
}