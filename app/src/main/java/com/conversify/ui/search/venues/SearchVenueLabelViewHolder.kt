package com.conversify.ui.search.venues

import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import kotlinx.android.synthetic.main.item_venue_your_venues_label.view.*

class SearchVenueLabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    init {
        itemView.tvLabelYourVenues.text=itemView.context.getString(R.string.search_top_label_suggested)
    }
}