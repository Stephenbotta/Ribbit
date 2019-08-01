package com.pulse.ui.search.tag

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.pulse.R
import com.pulse.data.remote.models.loginsignup.ProfileDto
import com.pulse.extensions.inflate
import com.pulse.utils.GlideRequests

class SearchTagAdapter(private val glide: GlideRequests,
                       private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_MY_VENUE = 0
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE_MY_VENUE -> SearchTagViewHolder(parent.inflate(R.layout.item_tag_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchTagViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item is ProfileDto) {
            return VIEW_TYPE_MY_VENUE
        }
        return VIEW_TYPE_MY_VENUE
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(item: List<Any>) {
        val oldListSize = this.items.size
        this.items.addAll(item)
        notifyItemRangeInserted(oldListSize, item.size)
    }

    fun getUpdatedList():MutableList<Any>{
        return items
    }

    interface Callback : SearchTagViewHolder.Callback
}