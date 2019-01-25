package com.conversify.ui.search.posts

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.models.venues.YourVenuesDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests

class SearchPostAdapter(private val glide: GlideRequests,
                        private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_MY_VENUE = 0
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE_MY_VENUE -> SearchPostViewHolder(parent.inflate(R.layout.item_post_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SearchPostViewHolder -> {
                if (item is GroupPostDto) {
                    holder.bind(item)
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item is GroupPostDto) {
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

    interface Callback : SearchPostViewHolder.Callback
}