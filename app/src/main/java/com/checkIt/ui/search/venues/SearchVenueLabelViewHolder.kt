package com.checkIt.ui.search.venues

import android.view.View
import com.checkIt.R
import kotlinx.android.synthetic.main.item_venue_your_venues_label.view.*

class SearchVenueLabelViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    init {
        itemView.tvLabelYourVenues.text=itemView.context.getString(R.string.search_top_label_suggested)
    }
}