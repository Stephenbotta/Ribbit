package com.conversify.ui.venues.filters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.conversify.R
import com.conversify.data.local.models.VenueFilters
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.inflate
import com.conversify.utils.DateTimeUtils
import kotlinx.android.synthetic.main.item_venue_filter_category.view.*
import kotlinx.android.synthetic.main.item_venue_filter_date.view.*
import kotlinx.android.synthetic.main.item_venue_filter_location.view.*
import kotlinx.android.synthetic.main.item_venue_filter_privacy.view.*

class FiltersCategoryAdapter(private val callback: Callback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_PRIVACY = 2
        private const val VIEW_TYPE_LOCATION = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(parent.inflate(R.layout.item_venue_filter_category), callback)

            VIEW_TYPE_DATE -> DateViewHolder(parent.inflate(R.layout.item_venue_filter_date), callback)

            VIEW_TYPE_PRIVACY -> PrivacyViewHolder(parent.inflate(R.layout.item_venue_filter_privacy), callback)

            VIEW_TYPE_LOCATION -> LocationViewHolder(parent.inflate(R.layout.item_venue_filter_location), callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is CategoryViewHolder -> {
                if (item is InterestDto) {
                    holder.bind(item)
                }
            }

            is DateViewHolder -> {
                if (item is VenueFilters.Date) {
                    holder.bind(item)
                }
            }

            is LocationViewHolder -> {
                if (item is VenueFilters.Location) {
                    holder.bind(item)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is InterestDto -> VIEW_TYPE_CATEGORY
            is VenueFilters.Date -> VIEW_TYPE_DATE
            is VenueFilters.Privacy -> VIEW_TYPE_PRIVACY
            else -> VIEW_TYPE_LOCATION
        }
    }

    fun displayItems(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View,
                             private val callback: CategoryViewHolder.Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { callback.onVenueCategoryClicked(category) }
        }

        private lateinit var category: InterestDto

        fun bind(category: InterestDto) {
            this.category = category
            itemView.tvCategory.text = category.name
        }

        interface Callback {
            fun onVenueCategoryClicked(category: InterestDto)
        }
    }

    class DateViewHolder(itemView: View,
                         private val callback: DateViewHolder.Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { callback.onSelectDateClicked() }
        }

        fun bind(date: VenueFilters.Date) {
            itemView.btnDate.text = DateTimeUtils.formatVenueFiltersDate(date.dateTimeMillisUtc)
        }

        interface Callback {
            fun onSelectDateClicked()
        }
    }

    class PrivacyViewHolder(itemView: View,
                            private val callback: PrivacyViewHolder.Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.btnPublic.setOnClickListener { callback.onPrivacySelected(false) }
            itemView.btnPrivate.setOnClickListener { callback.onPrivacySelected(true) }
        }

        interface Callback {
            fun onPrivacySelected(isPrivate: Boolean)
        }
    }

    class LocationViewHolder(itemView: View,
                             private val callback: LocationViewHolder.Callback) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { callback.onSelectLocationClicked() }
        }

        fun bind(location: VenueFilters.Location) {
            itemView.btnLocation.text = location.name
        }

        interface Callback {
            fun onSelectLocationClicked()
        }
    }

    interface Callback : CategoryViewHolder.Callback, DateViewHolder.Callback,
            PrivacyViewHolder.Callback, LocationViewHolder.Callback
}