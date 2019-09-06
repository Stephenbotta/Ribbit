package com.checkIt.ui.search.posts

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests

class SearchPostAdapter(private val glide: GlideRequests, private val callback: Callback) : RecyclerView.Adapter<SearchPostViewHolder>() {
    private val groups = mutableListOf<GroupPostDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPostViewHolder {
        return SearchPostViewHolder(parent.inflate(R.layout.item_post_search), glide, callback)
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: SearchPostViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    fun displayItems(items: List<GroupPostDto>) {
        this.groups.clear()
        this.groups.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(groups: List<GroupPostDto>) {
        val oldListSize = this.groups.size
        this.groups.addAll(groups)
        notifyItemRangeInserted(oldListSize, this.groups.size)
    }

    fun getUpdatedList(): MutableList<GroupPostDto> {
        return groups
    }

    interface Callback : SearchPostViewHolder.Callback
}