package com.checkIt.ui.search.posts

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests

class SearchPostAdapter(private val glide: GlideRequests,
                        private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<GroupPostDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchPostViewHolder(parent.inflate(R.layout.item_post_search), glide, callback)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchPostViewHolder -> {
                holder.bind(item)
            }
        }
    }

    fun displayItems(items: List<GroupPostDto>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(item: List<GroupPostDto>) {
        val oldListSize = this.items.size
        this.items.addAll(item)
        notifyItemRangeInserted(oldListSize, item.size)
    }

    fun getUpdatedList(): MutableList<GroupPostDto> {
        return items
    }

    interface Callback : SearchPostViewHolder.Callback
}