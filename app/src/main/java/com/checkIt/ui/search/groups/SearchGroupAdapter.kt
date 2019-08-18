package com.checkIt.ui.search.groups

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests

class SearchGroupAdapter(private val glide: GlideRequests,
                         private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> SearchGroupLabelViewHolder(parent.inflate(R.layout.item_venue_your_venues_label))

            VIEW_TYPE_MY_VENUE -> SearchGroupViewHolder(parent.inflate(R.layout.item_group_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchGroupViewHolder -> {
                if (item is GroupDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is GroupDto -> VIEW_TYPE_MY_VENUE
            else -> VIEW_TYPE_LABEL_YOUR_VENUES
        }
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

    fun getUpdatedList(): MutableList<Any> {
        return items
    }

    interface Callback : SearchGroupViewHolder.Callback
}