package com.checkIt.ui.createvenue

import android.view.View
import android.view.ViewGroup
import com.checkIt.R
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.data.remote.models.venues.VenueCategoriesHeader
import com.checkIt.extensions.inflate
import com.checkIt.utils.GlideRequests
import kotlinx.android.synthetic.main.item_create_group_venue_category.view.*

class VenueCategoriesAdapter(private val glide: GlideRequests,
                             private val callback: (InterestDto) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CATEGORY = 1
    }

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            ViewHolderHeader(parent.inflate(R.layout.item_create_venue_categories_header))
        } else {
            ViewHolderCategory(parent.inflate(R.layout.item_create_group_venue_category), glide, callback)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ViewHolderCategory && item is InterestDto) {
            holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is VenueCategoriesHeader) {
            TYPE_HEADER
        } else {
            TYPE_CATEGORY
        }
    }

    fun displayCategories(interests: List<InterestDto>) {
        items.removeAll { it !is VenueCategoriesHeader }
        items.addAll(interests)
        notifyDataSetChanged()
    }

    class ViewHolderHeader(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    class ViewHolderCategory(itemView: View,
                             private val glide: GlideRequests,
                             callback: (InterestDto) -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private lateinit var category: InterestDto

        init {
            itemView.setOnClickListener { callback(category) }
        }

        fun bind(category: InterestDto) {
            this.category = category

            glide.load(category.image?.thumbnail)
                    .error(R.color.greyImageBackground)
                    .placeholder(R.color.greyImageBackground)
                    .into(itemView.ivCategory)
            itemView.tvCategory.text = category.name
        }
    }
}