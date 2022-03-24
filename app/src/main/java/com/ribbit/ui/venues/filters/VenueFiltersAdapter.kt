package com.ribbit.ui.venues.filters

import android.view.View
import android.view.ViewGroup
import com.ribbit.R
import com.ribbit.data.local.models.VenueFilters
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.extensions.inflate
import com.ribbit.utils.DateTimeUtils
import kotlinx.android.synthetic.main.item_venue_filter_category.view.*
import kotlinx.android.synthetic.main.item_venue_filter_date.view.*
import kotlinx.android.synthetic.main.item_venue_filter_location.view.*
import kotlinx.android.synthetic.main.item_venue_filter_privacy.view.*

class VenueFiltersAdapter(private val callback: Callback) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_PRIVACY = 2
        private const val VIEW_TYPE_LOCATION = 3
    }

    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(parent.inflate(R.layout.item_venue_filter_category))

            VIEW_TYPE_DATE -> DateViewHolder(parent.inflate(R.layout.item_venue_filter_date), callback)

            VIEW_TYPE_PRIVACY -> PrivacyViewHolder(parent.inflate(R.layout.item_venue_filter_privacy))

            VIEW_TYPE_LOCATION -> LocationViewHolder(parent.inflate(R.layout.item_venue_filter_location), callback)

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
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

            is PrivacyViewHolder -> {
                if (item is VenueFilters.Privacy) {
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

    class CategoryViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        init {
            itemView.tvCategory.setOnClickListener {
                val checked = !itemView.tvCategory.isChecked
                category.selected = checked
                itemView.tvCategory.isChecked = checked
            }
        }

        private lateinit var category: InterestDto

        fun bind(category: InterestDto) {
            this.category = category
            itemView.tvCategory.text = category.name
            itemView.tvCategory.isChecked = category.selected
        }
    }

    class DateViewHolder(itemView: View,
                         private val callback: DateViewHolder.Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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

    class PrivacyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        init {
            itemView.btnPublic.setOnClickListener {
                val checked = !itemView.btnPublic.isChecked
                privacy.publicSelected = checked
                itemView.btnPublic.isChecked = checked
            }
            itemView.btnPrivate.setOnClickListener {
                val checked = !itemView.btnPrivate.isChecked
                privacy.privateSelected = checked
                itemView.btnPrivate.isChecked = checked
            }
        }

        private lateinit var privacy: VenueFilters.Privacy

        fun bind(privacy: VenueFilters.Privacy) {
            this.privacy = privacy
            itemView.btnPublic.isChecked = privacy.publicSelected
            itemView.btnPrivate.isChecked = privacy.privateSelected
        }
    }

    class LocationViewHolder(itemView: View,
                             private val callback: LocationViewHolder.Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
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

    interface Callback : DateViewHolder.Callback, LocationViewHolder.Callback
}