package com.conversify.ui.post.newpost

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.LoadingItem
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.post.ChooseGroupHeader
import com.conversify.extensions.inflate
import com.conversify.ui.post.details.viewholders.LoadingViewHolder
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_new_post_group.view.*

class ChooseGroupAdapter(private val glide: GlideRequests,
                         private val callback: (GroupDto) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_LOADING = 2
    }

    private val items = mutableListOf<Any>(ChooseGroupHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> ViewHolderHeader(parent.inflate(R.layout.item_new_post_choose_group_header))

            TYPE_LOADING -> LoadingViewHolder(parent.inflate(R.layout.item_loading))

            else -> ViewHolderGroup(parent.inflate(R.layout.item_new_post_group), glide, callback)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ViewHolderGroup && item is GroupDto) {
            holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChooseGroupHeader -> TYPE_HEADER
            is LoadingItem -> TYPE_LOADING
            else -> TYPE_GROUP
        }
    }

    fun displayGroups(groups: List<GroupDto>) {
        items.removeAll { it is GroupDto }
        items.addAll(groups)
        notifyDataSetChanged()
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            items.add(LoadingItem)
            notifyItemInserted(items.size - 1)
        } else {
            if (items.lastOrNull() is LoadingItem) {
                val loadingIndex = items.size - 1
                items.removeAt(loadingIndex)
                notifyItemRemoved(loadingIndex)
            }
        }
    }

    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ViewHolderGroup(itemView: View,
                          private val glide: GlideRequests,
                          callback: (GroupDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private lateinit var group: GroupDto

        init {
            itemView.setOnClickListener { callback(group) }
        }

        fun bind(group: GroupDto) {
            this.group = group
            glide.load(group.imageUrl?.thumbnail)
                    .into(itemView.ivGroup)
            itemView.tvGroupName.text = group.name
        }
    }
}