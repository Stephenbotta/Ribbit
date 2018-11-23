package com.conversify.ui.createvenue

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.venues.VenueCategoriesHeader
import com.conversify.extensions.inflate
import com.conversify.utils.GlideRequests
import kotlinx.android.synthetic.main.item_create_venue_category.view.*

class VenueCategoriesAdapter(private val glide: GlideRequests,
                             private val callback: (InterestDto) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CATEGORY = 1
    }

    private val items = mutableListOf<Any>(VenueCategoriesHeader)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            ViewHolderHeader(parent.inflate(R.layout.item_create_venue_header))
        } else {
            ViewHolderCategory(parent.inflate(R.layout.item_create_venue_category), glide, callback)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ViewHolderCategory(itemView: View,
                             private val glide: GlideRequests,
                             callback: (InterestDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
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