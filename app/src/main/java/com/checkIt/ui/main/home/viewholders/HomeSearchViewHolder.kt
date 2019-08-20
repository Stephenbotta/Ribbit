package com.checkIt.ui.main.home.viewholders

import android.view.View
import kotlinx.android.synthetic.main.item_home_search.view.*

class HomeSearchViewHolder(itemView: View,
                           callback: Callback) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    init {
        itemView.ivSearch.setOnClickListener { callback.onHomeSearchClicked() }
    }

    interface Callback {
        fun onHomeSearchClicked()
    }
}