package com.conversify.ui.profile.settings.blockusers

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests

class BlockUsersListAdapter(private val glide: GlideRequests,
                            private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE = 0
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE -> BlockUsersListViewHolder(parent.inflate(R.layout.item_top_search), glide, callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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