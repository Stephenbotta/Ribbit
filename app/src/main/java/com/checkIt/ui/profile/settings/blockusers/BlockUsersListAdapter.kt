package com.checkIt.ui.profile.settings.blockusers

import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests

class BlockUsersListAdapter(private val glide: GlideRequests,
                            private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE = 0
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE -> BlockUsersListViewHolder(parent.inflate(R.layout.item_top_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is BlockUsersListViewHolder -> {
                if (item is ProfileDto) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        return when (item) {
            is ProfileDto -> VIEW_TYPE
            else -> VIEW_TYPE
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

    interface Callback : BlockUsersListViewHolder.Callback
}