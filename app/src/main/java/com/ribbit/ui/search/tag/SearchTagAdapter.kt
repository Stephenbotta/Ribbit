package com.ribbit.ui.search.tag

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ribbit.R
import com.ribbit.ui.loginsignup.ProfileDto
import com.ribbit.extensions.inflate

class SearchTagAdapter(private val callback: Callback) : RecyclerView.Adapter<SearchTagViewHolder>() {
    private val tags = mutableListOf<ProfileDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTagViewHolder {
        return SearchTagViewHolder(parent.inflate(R.layout.item_tag_search), callback)
    }

    override fun getItemCount(): Int = tags.size

    override fun onBindViewHolder(holder: SearchTagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    fun displayItems(tags: List<ProfileDto>) {
        this.tags.clear()
        this.tags.addAll(tags)
        notifyDataSetChanged()
    }

    fun addMoreItems(tags: List<ProfileDto>) {
        val oldListSize = this.tags.size
        this.tags.addAll(tags)
        notifyItemRangeInserted(oldListSize, this.tags.size)
    }

    interface Callback : SearchTagViewHolder.Callback
}