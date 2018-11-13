package com.conversify.ui.venues.list

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.conversify.R
import com.conversify.extensions.inflate
import com.conversify.ui.venues.list.viewholder.YourVenuesViewHolder

class VenuesListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return YourVenuesViewHolder(parent.inflate(R.layout.item_your_venues))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }
}