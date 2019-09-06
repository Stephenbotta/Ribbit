package com.checkIt.ui.search.top

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests

class SearchTopAdapter(private val glide: GlideRequests, private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LABEL_YOUR_VENUES = 0
        private const val VIEW_TYPE_MY_VENUE = 1
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL_YOUR_VENUES -> SearchTopLabelViewHolder(parent.inflate(R.layout.item_venue_your_venues_label))

            VIEW_TYPE_MY_VENUE -> SearchTopViewHolder(parent.inflate(R.layout.item_top_search_square), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchTopViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProfileDto -> VIEW_TYPE_MY_VENUE
            else -> VIEW_TYPE_LABEL_YOUR_VENUES
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addMoreItems(item: List<Any>) {
        val oldListSize = items.size
        items.addAll(item)
        notifyItemRangeInserted(oldListSize, items.size)
    }

    interface Callback : SearchTopViewHolder.Callback
}