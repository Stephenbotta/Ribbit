package com.pulse.ui.main.home

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.groups.GroupPostDto
import com.pulse.data.remote.models.home.HomeSearchDto
import com.pulse.extensions.inflate
import com.pulse.ui.groups.GroupPostCallback
import com.pulse.ui.main.home.viewholders.HomePostViewHolder
import com.pulse.ui.main.home.viewholders.HomeSearchViewHolder
import com.pulse.utils.GlideRequests

class HomeAdapter(private val glide: GlideRequests,
                  private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SEARCH = 0
        private const val VIEW_TYPE_POST = 1
    }

    private val items = mutableListOf<Any>(HomeSearchDto)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SEARCH) {
            HomeSearchViewHolder(parent.inflate(R.layout.item_home_search), callback)
        } else {
            HomePostViewHolder(parent.inflate(R.layout.item_home_feed_post), glide, callback)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is HomeSearchDto) {
            VIEW_TYPE_SEARCH
        } else {
            VIEW_TYPE_POST
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HomePostViewHolder && item is GroupPostDto) {
            holder.bind(item)
        }
    }

    fun displayItems(posts: List<Any>) {
        this.items.clear()
        this.items.add(HomeSearchDto)
        this.items.addAll(posts)
        notifyDataSetChanged()
    }

    fun addItems(posts: List<Any>) {
        val oldListSize = this.items.size
        this.items.addAll(posts)
        notifyItemRangeInserted(oldListSize, posts.size)
    }

    fun isEmpty(): Boolean {
        return items.size == 1 && items.first() is HomeSearchDto
    }

    fun updatePost(updatedPost: GroupPostDto) {
        val postIndex = items.indexOfFirst { it is GroupPostDto && it.id == updatedPost.id }
        if (postIndex != -1) {
            items[postIndex] = updatedPost
            notifyItemChanged(postIndex)
        }
    }

    interface Callback : HomeSearchViewHolder.Callback, GroupPostCallback
}